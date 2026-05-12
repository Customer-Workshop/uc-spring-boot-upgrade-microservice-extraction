"""ALL routes for the Playwright Test Automation Platform."""

import json
import os
import re
import sys
import threading
import uuid
from datetime import datetime
from typing import Optional

from fastapi import APIRouter, Depends, Form, HTTPException, Request, UploadFile, File
from fastapi.responses import HTMLResponse, JSONResponse, RedirectResponse
from sqlalchemy.orm import Session
from sse_starlette.sse import EventSourceResponse

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from db.db_utils import (
    SessionLocal,
    add_log,
    create_endpoint,
    create_environment,
    create_project,
    create_scheduled_job,
    create_suite,
    create_test_case,
    create_user,
    delete_locator_field,
    delete_project,
    delete_run_logs,
    delete_scheduled_job,
    delete_suite,
    delete_test_case,
    get_all_environments,
    get_all_projects,
    get_all_scheduled_jobs,
    get_all_suites,
    get_all_test_cases,
    get_db,
    get_db_session,
    get_endpoint,
    get_endpoints_for_tc,
    get_locator_field,
    get_locator_fields_grouped,
    get_logs_for_run,
    get_logs_for_tc,
    get_project,
    get_runs_for_tc,
    get_scheduled_job,
    get_screenshots_for_run,
    get_steps,
    get_suite,
    get_test_case,
    get_test_data,
    get_user_by_username,
    save_steps,
    save_test_data,
    update_endpoint,
    update_tc_status,
    upsert_locator_field,
    upsert_symbolic_locator,
)
from db.models import (
    ApiEndpoint,
    ExecutionLog,
    LocatorField,
    ScheduledJob,
    TestCase,
    TestStep,
    TestSuite,
)

router = APIRouter()


def get_templates():
    from app.main import templates
    return templates


# --- Auth helpers ---

def get_current_user(request: Request) -> Optional[dict]:
    """Get current user from session."""
    return request.session.get("user")


def require_auth(request: Request):
    """Require authentication - redirect to login if not authenticated."""
    user = get_current_user(request)
    if not user:
        raise HTTPException(status_code=303, headers={"Location": "/login"})
    return user


def require_editor(request: Request):
    """Require editor or admin role."""
    user = require_auth(request)
    if user.get("role") == "viewer":
        raise HTTPException(status_code=403, detail="Viewer role cannot modify data")
    return user


# --- Navigation Routes ---

@router.get("/", response_class=HTMLResponse)
async def root(request: Request):
    return RedirectResponse(url="/automation", status_code=302)


@router.get("/web", response_class=HTMLResponse)
async def web_redirect():
    return RedirectResponse(url="/automation", status_code=302)


@router.get("/api", response_class=HTMLResponse)
async def api_redirect():
    return RedirectResponse(url="/automation", status_code=302)


# --- Auth Routes ---

@router.get("/login", response_class=HTMLResponse)
async def login_page(request: Request):
    templates = get_templates()
    return templates.TemplateResponse("login.html", {"request": request})


@router.post("/login")
async def login(request: Request, username: str = Form(...), password: str = Form(...)):
    from passlib.hash import bcrypt

    db = SessionLocal()
    try:
        user = get_user_by_username(db, username)
        if not user or not bcrypt.verify(password, user.password_hash):
            templates = get_templates()
            return templates.TemplateResponse("login.html", {"request": request, "error": "Invalid credentials"})

        request.session["user"] = {"id": user.id, "username": user.username, "role": user.role}
        return RedirectResponse(url="/automation", status_code=302)
    finally:
        db.close()


@router.get("/logout")
async def logout(request: Request):
    request.session.clear()
    return RedirectResponse(url="/login", status_code=302)


@router.post("/register")
async def register(request: Request, username: str = Form(...), password: str = Form(...), role: str = Form("editor")):
    from passlib.hash import bcrypt

    db = SessionLocal()
    try:
        existing = get_user_by_username(db, username)
        if existing:
            templates = get_templates()
            return templates.TemplateResponse("login.html", {"request": request, "error": "Username already exists"})

        password_hash = bcrypt.hash(password)
        create_user(db, username, password_hash, role)
        request.session["user"] = {"id": 1, "username": username, "role": role}
        return RedirectResponse(url="/automation", status_code=302)
    finally:
        db.close()


# --- Main Hub ---

@router.get("/automation", response_class=HTMLResponse)
async def automation_hub(request: Request, db: Session = Depends(get_db)):
    templates = get_templates()
    projects = get_all_projects(db)
    suites = get_all_suites(db)
    test_cases = get_all_test_cases(db)

    # Build stats
    stats = {
        "total": len(test_cases),
        "passed": sum(1 for tc in test_cases if tc.status == "PASS"),
        "failed": sum(1 for tc in test_cases if tc.status == "FAIL"),
        "pending": sum(1 for tc in test_cases if tc.status == "PENDING"),
        "web": sum(1 for tc in test_cases if tc.flow_type == "web"),
        "api": sum(1 for tc in test_cases if tc.flow_type == "api"),
    }

    # Serialize suites data for JS
    suites_data = []
    for s in suites:
        suite_tcs = [tc for tc in test_cases if tc.suite_id == s.id]
        suites_data.append({
            "id": s.id,
            "name": s.name,
            "flow_type": s.flow_type,
            "project_id": s.project_id,
            "description": s.description or "",
            "test_cases": [{
                "id": tc.id,
                "name": tc.name,
                "flow_type": tc.flow_type,
                "status": tc.status,
                "created_at": tc.created_at.strftime("%Y-%m-%d %H:%M") if tc.created_at else "",
            } for tc in suite_tcs],
        })

    return templates.TemplateResponse("automation.html", {
        "request": request,
        "projects": projects,
        "suites": suites,
        "test_cases": test_cases,
        "stats": stats,
        "suites_data": suites_data,
        "user": get_current_user(request),
    })


# --- Project Routes ---

@router.post("/project/create")
async def create_project_route(request: Request, name: str = Form(...), description: str = Form("")):
    db = SessionLocal()
    try:
        create_project(db, name, description)
    finally:
        db.close()
    return RedirectResponse(url="/automation", status_code=302)


@router.post("/project/{project_id}/delete")
async def delete_project_route(project_id: int):
    db = SessionLocal()
    try:
        delete_project(db, project_id)
    finally:
        db.close()
    return RedirectResponse(url="/automation", status_code=302)


# --- Suite Routes ---

@router.post("/suite/create")
async def create_suite_route(request: Request, name: str = Form(...), flow_type: str = Form("web"),
                             description: str = Form(""), project_id: int = Form(None)):
    db = SessionLocal()
    try:
        create_suite(db, name, flow_type, description, project_id if project_id else None)
    finally:
        db.close()
    return RedirectResponse(url="/automation", status_code=302)


@router.post("/suite/{suite_id}/delete")
async def delete_suite_route(suite_id: int):
    db = SessionLocal()
    try:
        delete_suite(db, suite_id)
    finally:
        db.close()
    return RedirectResponse(url="/automation", status_code=302)


@router.get("/suite/{suite_id}/export")
async def export_suite(suite_id: int, db: Session = Depends(get_db)):
    """Export suite as JSON with all TCs, steps, endpoints, test data."""
    suite = get_suite(db, suite_id)
    if not suite:
        raise HTTPException(status_code=404, detail="Suite not found")

    export_data = {
        "name": suite.name,
        "flow_type": suite.flow_type,
        "description": suite.description or "",
        "test_cases": [],
    }

    for tc in suite.test_cases:
        tc_data = {
            "name": tc.name,
            "flow_type": tc.flow_type,
            "base_url": tc.base_url or "",
            "steps": [{"order": s.order, "action": s.action, "selector": s.selector, "value": s.value, "description": s.description} for s in tc.steps],
            "test_data": [{"row_index": td.row_index, "data_json": td.data_json} for td in tc.test_data],
            "endpoints": [{
                "method": ep.method, "path": ep.path, "summary": ep.summary,
                "request_headers": ep.request_headers, "request_params": ep.request_params,
                "request_body": ep.request_body, "expected_status": ep.expected_status,
            } for ep in tc.endpoints],
        }
        export_data["test_cases"].append(tc_data)

    return JSONResponse(content=export_data, headers={"Content-Disposition": f'attachment; filename="suite_{suite.name}.json"'})


@router.post("/suite/import")
async def import_suite(request: Request, file: UploadFile = File(...), project_id: int = Form(None)):
    """Import suite from JSON file."""
    db = SessionLocal()
    try:
        content = await file.read()
        data = json.loads(content)

        suite = create_suite(db, data["name"], data.get("flow_type", "web"), data.get("description", ""), project_id)

        for tc_data in data.get("test_cases", []):
            tc = create_test_case(db, suite.id, tc_data["name"], tc_data.get("flow_type", "web"), tc_data.get("base_url", ""))

            if tc_data.get("steps"):
                save_steps(db, tc.id, tc_data["steps"])
            if tc_data.get("test_data"):
                save_test_data(db, tc.id, tc_data["test_data"])
            for ep_data in tc_data.get("endpoints", []):
                create_endpoint(db, tc.id, **ep_data)

        return RedirectResponse(url="/automation", status_code=302)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Import failed: {str(e)}")
    finally:
        db.close()


# --- TestCase Routes ---

@router.post("/testcase/create")
async def create_testcase_route(request: Request, suite_id: int = Form(...), name: str = Form(...),
                                flow_type: str = Form("web"), base_url: str = Form("")):
    db = SessionLocal()
    try:
        tc = create_test_case(db, suite_id, name, flow_type, base_url)
        if flow_type == "api":
            return RedirectResponse(url=f"/api/flow/{tc.id}", status_code=302)
        return RedirectResponse(url=f"/web/flow/{tc.id}", status_code=302)
    finally:
        db.close()


@router.post("/testcase/{tc_id}/delete")
async def delete_testcase_route(tc_id: int):
    db = SessionLocal()
    try:
        delete_test_case(db, tc_id)
    finally:
        db.close()
    return RedirectResponse(url="/automation", status_code=302)


@router.post("/testcase/{tc_id}/move")
async def move_testcase(tc_id: int, target_suite_id: int = Form(...)):
    db = SessionLocal()
    try:
        tc = get_test_case(db, tc_id)
        if tc:
            tc.suite_id = target_suite_id
            db.commit()
    finally:
        db.close()
    return RedirectResponse(url="/automation", status_code=302)


@router.post("/testcase/{tc_id}/copy")
async def copy_testcase(tc_id: int, target_suite_id: int = Form(...), new_name: str = Form("")):
    db = SessionLocal()
    try:
        tc = get_test_case(db, tc_id)
        if tc:
            name = new_name or f"{tc.name} (copy)"
            new_tc = create_test_case(db, target_suite_id, name, tc.flow_type, tc.base_url)

            # Copy steps
            steps = get_steps(db, tc_id)
            if steps:
                step_dicts = [{"order": s.order, "action": s.action, "selector": s.selector, "value": s.value, "description": s.description} for s in steps]
                save_steps(db, new_tc.id, step_dicts)

            # Copy test data
            data = get_test_data(db, tc_id)
            if data:
                data_dicts = [{"row_index": d.row_index, "data_json": d.data_json} for d in data]
                save_test_data(db, new_tc.id, data_dicts)

            # Copy endpoints
            endpoints = get_endpoints_for_tc(db, tc_id)
            for ep in endpoints:
                create_endpoint(db, new_tc.id,
                                method=ep.method, path=ep.path, summary=ep.summary,
                                request_headers=ep.request_headers, request_params=ep.request_params,
                                request_body=ep.request_body, expected_status=ep.expected_status)
    finally:
        db.close()
    return RedirectResponse(url="/automation", status_code=302)


# --- Web Flow Routes ---

@router.get("/web/flow/{tc_id}", response_class=HTMLResponse)
async def web_flow_page(request: Request, tc_id: int, db: Session = Depends(get_db)):
    templates = get_templates()
    tc = get_test_case(db, tc_id)
    if not tc:
        raise HTTPException(status_code=404, detail="Test case not found")

    steps = get_steps(db, tc_id)
    test_data = get_test_data(db, tc_id)

    steps_data = [{"id": s.id, "order": s.order, "action": s.action, "selector": s.selector, "value": s.value, "description": s.description} for s in steps]
    data_rows = [{"row_index": d.row_index, "data_json": d.data_json} for d in test_data]

    return templates.TemplateResponse("web_flow.html", {
        "request": request,
        "tc": tc,
        "steps": steps_data,
        "test_data": data_rows,
        "user": get_current_user(request),
    })


@router.post("/web/start-recording")
async def start_recording(request: Request, tc_id: int = Form(...), base_url: str = Form("")):
    from agent.state_machine import agent
    from playwright_integration.recorder import launch_codegen

    try:
        agent.start_recording()
    except Exception as e:
        return JSONResponse({"error": f"Cannot start recording: {str(e)}"}, status_code=400)

    output_path = f"recorded_tc_{tc_id}.py"
    launch_codegen(base_url or "https://example.com", output_path)

    return JSONResponse({"status": "recording", "tc_id": tc_id})


@router.post("/web/stop-recording")
async def stop_recording(request: Request):
    from agent.state_machine import agent
    from playwright_integration.recorder import parse_codegen_script, stop_codegen

    try:
        agent.stop_recording()
    except Exception:
        pass

    script_path = stop_codegen()

    try:
        agent.parsing_done()
    except Exception:
        pass

    steps = parse_codegen_script(script_path) if script_path else []
    return JSONResponse({"status": "done", "steps": steps})


@router.post("/web/save-steps/{tc_id}")
async def save_steps_route(tc_id: int, request: Request):
    body = await request.json()
    db = SessionLocal()
    try:
        save_steps(db, tc_id, body)
    finally:
        db.close()
    return JSONResponse({"status": "ok"})


@router.post("/web/save-data/{tc_id}")
async def save_data_route(tc_id: int, request: Request):
    body = await request.json()
    db = SessionLocal()
    try:
        save_test_data(db, tc_id, body)
    finally:
        db.close()
    return JSONResponse({"status": "ok"})


# --- API Flow Routes ---

@router.get("/api/flow/{tc_id}", response_class=HTMLResponse)
async def api_flow_page(request: Request, tc_id: int, db: Session = Depends(get_db)):
    templates = get_templates()
    tc = get_test_case(db, tc_id)
    if not tc:
        raise HTTPException(status_code=404, detail="Test case not found")

    endpoints = get_endpoints_for_tc(db, tc_id)
    endpoints_data = [{
        "id": ep.id,
        "method": ep.method,
        "path": ep.path,
        "summary": ep.summary,
        "request_headers": ep.request_headers or "{}",
        "request_params": ep.request_params or "{}",
        "request_body": ep.request_body or "{}",
        "expected_status": ep.expected_status,
        "actual_status": ep.actual_status,
        "response_body": ep.response_body or "",
        "passed": ep.passed,
    } for ep in endpoints]

    status_cls = tc.status.lower() if tc.status in ["PASS", "FAIL"] else "pending"

    return templates.TemplateResponse("api_flow.html", {
        "request": request,
        "tc": tc,
        "endpoints": endpoints_data,
        "status_cls": status_cls,
        "user": get_current_user(request),
    })


@router.post("/api/parse-swagger/{tc_id}")
async def parse_swagger_route(tc_id: int, request: Request,
                              swagger_url: str = Form(None), swagger_file: UploadFile = File(None)):
    from agent.state_machine import agent
    from agent.swagger_parser import generate_negative_variants, generate_performance_variants, parse_swagger

    try:
        agent.start_swagger()
    except Exception:
        agent.reset()
        agent.start_swagger()

    db = SessionLocal()
    try:
        tc = get_test_case(db, tc_id)
        if not tc:
            raise HTTPException(status_code=404, detail="Test case not found")

        # Determine source
        source = ""
        if swagger_url:
            source = swagger_url
        elif swagger_file:
            content = await swagger_file.read()
            temp_path = f"/tmp/swagger_{tc_id}.yaml"
            with open(temp_path, "wb") as f:
                f.write(content)
            source = temp_path

        if not source:
            raise HTTPException(status_code=400, detail="No swagger URL or file provided")

        # Parse endpoints
        endpoints = parse_swagger(source)
        if not endpoints:
            agent.safe_fail("No endpoints parsed")
            return JSONResponse({"error": "Failed to parse swagger spec"}, status_code=400)

        # Save positive endpoints to current TC
        for ep_data in endpoints:
            create_endpoint(db, tc_id, **ep_data)

        # Generate and save negative variants
        neg_variants = generate_negative_variants(endpoints)
        neg_tc = create_test_case(db, tc.suite_id, f"[Negative] {tc.name}", "api", tc.base_url)
        for ep_data in neg_variants:
            create_endpoint(db, neg_tc.id, **ep_data)

        # Generate and save performance variants
        perf_variants = generate_performance_variants(endpoints)
        perf_tc = create_test_case(db, tc.suite_id, f"[Performance] {tc.name}", "api", tc.base_url)
        for ep_data in perf_variants:
            create_endpoint(db, perf_tc.id, **ep_data)

        try:
            agent.swagger_done()
        except Exception:
            pass

        return JSONResponse({
            "endpoints": len(endpoints),
            "neg_tc_id": neg_tc.id,
            "neg_count": len(neg_variants),
            "perf_tc_id": perf_tc.id,
            "perf_count": len(perf_variants),
        })
    finally:
        db.close()


@router.post("/api/save-endpoint/{ep_id}")
async def save_endpoint_route(ep_id: int, request: Request):
    body = await request.json()
    db = SessionLocal()
    try:
        update_data = {}
        for key in ["method", "path", "summary", "expected_status", "request_headers", "request_params", "request_body"]:
            if key in body:
                val = body[key]
                if key in ("request_headers", "request_params", "request_body"):
                    val = val if isinstance(val, str) else json.dumps(val)
                update_data[key] = val

        # Handle TC-level updates
        if "tc_name" in body or "tc_baseurl" in body:
            ep = get_endpoint(db, ep_id)
            if ep:
                tc = get_test_case(db, ep.test_case_id)
                if tc:
                    if "tc_name" in body:
                        tc.name = body["tc_name"]
                    if "tc_baseurl" in body:
                        tc.base_url = body["tc_baseurl"]
                    db.commit()

        update_endpoint(db, ep_id, **update_data)
        return JSONResponse({"status": "ok"})
    finally:
        db.close()


@router.post("/api/retest-endpoint/{ep_id}")
async def retest_endpoint(ep_id: int):
    db = SessionLocal()
    try:
        ep = get_endpoint(db, ep_id)
        if not ep:
            raise HTTPException(status_code=404, detail="Endpoint not found")

        tc_id = ep.test_case_id

        def _run():
            from agent.symbolic_runner import run_test_case
            run_test_case(tc_id, headless=True)

        thread = threading.Thread(target=_run, daemon=True)
        thread.start()

        return JSONResponse({"status": "running", "tc_id": tc_id})
    finally:
        db.close()


@router.get("/api/endpoint-logs/{ep_id}")
async def get_endpoint_logs(ep_id: int, db: Session = Depends(get_db)):
    ep = get_endpoint(db, ep_id)
    if not ep:
        return JSONResponse([])

    tc_id = ep.test_case_id
    logs = get_logs_for_tc(db, tc_id)

    # Get most recent run
    run_ids = list(set(log.run_id for log in logs))
    if not run_ids:
        return JSONResponse([])

    latest_run = run_ids[-1]
    run_logs = [log for log in logs if log.run_id == latest_run]

    result = []
    for log in run_logs:
        relevant = ep.path in (log.message or "") or ep.method in (log.message or "")
        result.append({
            "ts": log.timestamp.strftime("%H:%M:%S.%f")[:-3] if log.timestamp else "",
            "level": log.level or "INFO",
            "state": log.agent_state or "",
            "msg": log.message or "",
            "dur": log.duration_ms or 0,
            "relevant": relevant,
        })

    return JSONResponse(result)


@router.get("/api/tc-status/{tc_id}")
async def get_tc_status(tc_id: int, db: Session = Depends(get_db)):
    tc = get_test_case(db, tc_id)
    if not tc:
        raise HTTPException(status_code=404, detail="Test case not found")
    return JSONResponse({"tc_id": tc_id, "status": tc.status})


# --- Execute Routes ---

@router.get("/execute", response_class=HTMLResponse)
async def execute_page(request: Request, db: Session = Depends(get_db)):
    templates = get_templates()
    test_cases = get_all_test_cases(db)
    environments = get_all_environments(db)
    scheduled_jobs = get_all_scheduled_jobs(db)

    return templates.TemplateResponse("execute.html", {
        "request": request,
        "test_cases": test_cases,
        "environments": environments,
        "scheduled_jobs": scheduled_jobs,
        "user": get_current_user(request),
    })


@router.post("/execute/run")
async def execute_run(request: Request, tc_ids: str = Form(...), headless: str = Form("true")):
    from agent.state_machine import agent
    from agent.symbolic_runner import run_test_case

    is_headless = headless.lower() in ("true", "1", "on")
    ids = [int(x.strip()) for x in tc_ids.split(",") if x.strip().isdigit()]

    if not ids:
        return JSONResponse({"error": "No test case IDs provided"}, status_code=400)

    try:
        agent.start_execution()
    except Exception:
        agent.reset()
        agent.start_execution()

    def _run_all():
        for tc_id in ids:
            run_test_case(tc_id, headless=is_headless)
        try:
            agent.execution_done()
            agent.report_done()
        except Exception:
            agent.reset()

    thread = threading.Thread(target=_run_all, daemon=True)
    thread.start()

    return JSONResponse({"status": "running", "tc_ids": ids})


@router.get("/execute/status/{tc_id}")
async def execute_status(tc_id: int, db: Session = Depends(get_db)):
    tc = get_test_case(db, tc_id)
    if not tc:
        raise HTTPException(status_code=404, detail="Test case not found")
    return JSONResponse({"status": tc.status})


# --- SSE Streaming ---

@router.get("/execute/stream/{tc_id}")
async def stream_logs(tc_id: int, request: Request):
    """Server-Sent Events endpoint for live log streaming during execution."""
    import asyncio

    async def event_generator():
        last_id = 0
        polls = 0
        while polls < 120:
            polls += 1
            db = SessionLocal()
            try:
                tc = get_test_case(db, tc_id)
                logs = get_logs_for_tc(db, tc_id)

                new_logs = [l for l in logs if l.id > last_id]
                for log in new_logs:
                    last_id = log.id
                    data = json.dumps({
                        "id": log.id,
                        "level": log.level,
                        "message": log.message or "",
                        "agent_state": log.agent_state or "",
                        "duration_ms": log.duration_ms,
                        "timestamp": log.timestamp.strftime("%H:%M:%S.%f")[:-3] if log.timestamp else "",
                    })
                    yield {"event": "log", "data": data}

                if tc and tc.status not in ("RUNNING", "PENDING"):
                    yield {"event": "done", "data": json.dumps({"status": tc.status})}
                    return
            finally:
                db.close()

            await asyncio.sleep(1)

        yield {"event": "timeout", "data": json.dumps({"message": "Stream timeout"})}

    return EventSourceResponse(event_generator())


# --- Report Routes ---

@router.get("/report", response_class=HTMLResponse)
async def report_page(request: Request, db: Session = Depends(get_db)):
    templates = get_templates()
    test_cases = get_all_test_cases(db)

    # Collect all runs
    runs = []
    for tc in test_cases:
        tc_logs = get_logs_for_tc(db, tc.id)
        run_ids = list(set(log.run_id for log in tc_logs))
        for run_id in run_ids:
            run_logs = [l for l in tc_logs if l.run_id == run_id]
            if run_logs:
                start_time = run_logs[0].timestamp
                end_time = run_logs[-1].timestamp
                duration = (end_time - start_time).total_seconds() if start_time and end_time else 0

                # Compute run status
                run_status = _compute_run_status(run_logs)

                runs.append({
                    "run_id": run_id,
                    "tc_id": tc.id,
                    "tc_name": tc.name,
                    "flow_type": tc.flow_type,
                    "status": run_status,
                    "started": start_time.strftime("%Y-%m-%d %H:%M:%S") if start_time else "",
                    "duration": f"{duration:.1f}s",
                })

    runs.sort(key=lambda r: r["started"], reverse=True)

    return templates.TemplateResponse("report.html", {
        "request": request,
        "runs": runs,
        "user": get_current_user(request),
    })


@router.get("/report/{tc_id}", response_class=HTMLResponse)
async def report_detail_page(request: Request, tc_id: int, run_id: str = None, db: Session = Depends(get_db)):
    templates = get_templates()
    tc = get_test_case(db, tc_id)
    if not tc:
        raise HTTPException(status_code=404, detail="Test case not found")

    all_runs = get_runs_for_tc(db, tc_id)
    if not all_runs:
        return templates.TemplateResponse("report_detail.html", {
            "request": request,
            "tc": tc,
            "runs": [],
            "selected_run": None,
            "logs": [],
            "screenshots": [],
            "endpoints": [],
            "steps": [],
            "run_status": "PENDING",
            "user": get_current_user(request),
        })

    selected_run = run_id or all_runs[-1]
    logs = get_logs_for_run(db, selected_run)
    screenshots = get_screenshots_for_run(db, selected_run)
    endpoints = get_endpoints_for_tc(db, tc_id)
    steps = get_steps(db, tc_id)

    # Compute run-specific status
    run_status = _compute_run_status(logs)

    # Build run selector data with status
    runs_data = []
    for rid in reversed(all_runs):
        rlogs = get_logs_for_run(db, rid)
        rstatus = _compute_run_status(rlogs)
        runs_data.append({"run_id": rid, "status": rstatus})

    logs_data = [{
        "id": l.id,
        "level": l.level or "INFO",
        "message": l.message or "",
        "agent_state": l.agent_state or "",
        "duration_ms": l.duration_ms,
        "timestamp": l.timestamp.strftime("%H:%M:%S.%f")[:-3] if l.timestamp else "",
    } for l in logs]

    ep_data = [{
        "id": ep.id,
        "method": ep.method,
        "path": ep.path,
        "summary": ep.summary,
        "expected_status": ep.expected_status,
        "actual_status": ep.actual_status,
        "response_body": ep.response_body or "",
        "passed": ep.passed,
        "request_headers": ep.request_headers or "{}",
        "request_params": ep.request_params or "{}",
        "request_body": ep.request_body or "{}",
    } for ep in endpoints]

    steps_data = [{"order": s.order, "action": s.action, "selector": s.selector, "value": s.value, "description": s.description} for s in steps]

    return templates.TemplateResponse("report_detail.html", {
        "request": request,
        "tc": tc,
        "runs": runs_data,
        "selected_run": selected_run,
        "logs": logs_data,
        "screenshots": screenshots,
        "endpoints": ep_data,
        "steps": steps_data,
        "run_status": run_status,
        "user": get_current_user(request),
    })


@router.post("/report/{tc_id}/run/{run_id}/delete")
async def delete_tc_run(tc_id: int, run_id: str):
    db = SessionLocal()
    try:
        delete_run_logs(db, run_id, tc_id)
    finally:
        db.close()
    return RedirectResponse(url=f"/report/{tc_id}", status_code=302)


@router.post("/report/run/{run_id}/delete")
async def delete_run_global(run_id: str):
    db = SessionLocal()
    try:
        delete_run_logs(db, run_id)
    finally:
        db.close()
    return RedirectResponse(url="/report", status_code=302)


@router.post("/report/runs/delete-selected")
async def delete_selected_runs(request: Request, run_ids: str = Form(...)):
    db = SessionLocal()
    try:
        ids = [r.strip() for r in run_ids.split(",") if r.strip()]
        for rid in ids:
            delete_run_logs(db, rid)
    finally:
        db.close()
    return RedirectResponse(url="/report", status_code=302)


# --- Locator Routes ---

@router.get("/locators", response_class=HTMLResponse)
async def locators_page(request: Request, db: Session = Depends(get_db)):
    templates = get_templates()
    grouped = get_locator_fields_grouped(db)

    # Convert to serializable format
    grouped_data = {}
    for app, pages in grouped.items():
        grouped_data[app] = {}
        for page, fields in pages.items():
            grouped_data[app][page] = [{
                "id": f.id, "field_name": f.field_name,
                "css_selector": f.css_selector, "xpath": f.xpath,
                "id_attr": f.id_attr, "name_attr": f.name_attr,
                "text_content": f.text_content, "role_attr": f.role_attr,
                "test_id": f.test_id, "placeholder": f.placeholder,
            } for f in fields]

    return templates.TemplateResponse("locators.html", {
        "request": request,
        "grouped": grouped_data,
        "user": get_current_user(request),
    })


@router.post("/locators/save")
async def save_locator(request: Request, abstract_name: str = Form(...),
                       selector: str = Form(""), page_context: str = Form("")):
    db = SessionLocal()
    try:
        upsert_symbolic_locator(db, abstract_name, selector, page_context)
    finally:
        db.close()
    return RedirectResponse(url="/locators", status_code=302)


@router.post("/locators/field/save")
async def save_locator_field(request: Request):
    body = await request.json()
    db = SessionLocal()
    try:
        upsert_locator_field(db, **body)
        return JSONResponse({"status": "ok"})
    finally:
        db.close()


@router.post("/locators/field/delete/{field_id}")
async def delete_locator_field_route(field_id: int):
    db = SessionLocal()
    try:
        delete_locator_field(db, field_id)
    finally:
        db.close()
    return JSONResponse({"status": "ok"})


@router.get("/locators/field/{field_id}")
async def get_locator_field_route(field_id: int, db: Session = Depends(get_db)):
    field = get_locator_field(db, field_id)
    if not field:
        raise HTTPException(status_code=404, detail="Field not found")
    return JSONResponse({
        "id": field.id, "app_name": field.app_name, "page_name": field.page_name,
        "field_name": field.field_name, "css_selector": field.css_selector,
        "xpath": field.xpath, "id_attr": field.id_attr, "name_attr": field.name_attr,
        "text_content": field.text_content, "role_attr": field.role_attr,
        "test_id": field.test_id, "placeholder": field.placeholder,
    })


@router.get("/locators/fields/grouped")
async def get_locators_grouped(db: Session = Depends(get_db)):
    grouped = get_locator_fields_grouped(db)
    result = {}
    for app, pages in grouped.items():
        result[app] = {}
        for page, fields in pages.items():
            result[app][page] = [{
                "id": f.id, "field_name": f.field_name,
                "css_selector": f.css_selector, "xpath": f.xpath,
                "id_attr": f.id_attr, "name_attr": f.name_attr,
                "text_content": f.text_content, "role_attr": f.role_attr,
                "test_id": f.test_id, "placeholder": f.placeholder,
            } for f in fields]
    return JSONResponse(result)


@router.post("/locators/sync/{tc_id}")
async def sync_locators(tc_id: int, db: Session = Depends(get_db)):
    """Auto-extract selectors from TestSteps into LocatorField table."""
    tc = get_test_case(db, tc_id)
    if not tc:
        raise HTTPException(status_code=404, detail="Test case not found")

    steps = get_steps(db, tc_id)
    count = 0
    for step in steps:
        if step.selector:
            upsert_locator_field(db,
                                 app_name=tc.name,
                                 page_name="Auto-synced",
                                 field_name=step.description or f"Step {step.order}",
                                 css_selector=step.selector)
            count += 1

    return JSONResponse({"status": "ok", "synced": count})


# --- Agent Routes ---

@router.get("/agent/status")
async def agent_status():
    from agent.state_machine import agent
    return JSONResponse(agent.status_dict())


@router.post("/agent/reset")
async def agent_reset():
    from agent.state_machine import agent
    agent.reset()
    return JSONResponse({"status": "ok", "state": "IDLE"})


# --- Scheduled Execution Routes ---

@router.post("/schedule/create")
async def create_schedule(request: Request, tc_id: int = Form(...),
                          cron_expr: str = Form(...), notify_on_failure: str = Form("")):
    db = SessionLocal()
    try:
        job = create_scheduled_job(db, tc_id, cron_expr, notify_on_failure)
        # Register with APScheduler
        try:
            from app.scheduler import add_job
            add_job(job.id, tc_id, cron_expr)
        except Exception:
            pass
        return RedirectResponse(url="/execute", status_code=302)
    finally:
        db.close()


@router.post("/schedule/{job_id}/delete")
async def delete_schedule(job_id: int):
    db = SessionLocal()
    try:
        try:
            from app.scheduler import remove_job
            remove_job(job_id)
        except Exception:
            pass
        delete_scheduled_job(db, job_id)
    finally:
        db.close()
    return RedirectResponse(url="/execute", status_code=302)


@router.post("/schedule/{job_id}/toggle")
async def toggle_schedule(job_id: int):
    db = SessionLocal()
    try:
        job = get_scheduled_job(db, job_id)
        if job:
            job.enabled = not job.enabled
            db.commit()
            try:
                from app.scheduler import toggle_job
                toggle_job(job_id, job.enabled)
            except Exception:
                pass
    finally:
        db.close()
    return JSONResponse({"status": "ok"})


# --- Environment Routes ---

@router.post("/environment/create")
async def create_env_route(request: Request, name: str = Form(...),
                           base_url: str = Form(...), auth_headers: str = Form("{}")):
    db = SessionLocal()
    try:
        create_environment(db, name, base_url, auth_headers)
    finally:
        db.close()
    return RedirectResponse(url="/execute", status_code=302)


# --- Helper Functions ---

def _compute_run_status(logs) -> str:
    """Compute run status from log messages (NOT from tc.status)."""
    for log in reversed(logs):
        msg = log.message if hasattr(log, "message") else log.get("message", "")
        if "FAIL=" in msg:
            m = re.search(r"FAIL=(\d+)", msg)
            if m:
                return "PASS" if int(m.group(1)) == 0 else "FAIL"
    # Fallback: check for ERROR level
    for log in logs:
        level = log.level if hasattr(log, "level") else log.get("level", "")
        if level == "ERROR":
            return "FAIL"
    return "PASS"
