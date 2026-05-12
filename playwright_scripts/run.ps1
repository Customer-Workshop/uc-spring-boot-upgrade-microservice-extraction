# Start: uvicorn app.main:app --host 0.0.0.0 --port 8005 --reload
Set-Location $PSScriptRoot
python -m uvicorn app.main:app --host 0.0.0.0 --port 8005 --reload
