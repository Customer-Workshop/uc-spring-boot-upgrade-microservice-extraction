import Head from "next/head";
import React from "react";
import useSWR from "swr";

import ErrorMessage from "../components/common/ErrorMessage";
import LoadingSpinner from "../components/common/LoadingSpinner";
import SystemAPI from "../lib/api/system";
import { Readiness, ReadinessCheck } from "../lib/types/readinessType";

const fetchReadiness = async (): Promise<Readiness> => {
  const { data } = await SystemAPI.getReadiness();
  return data.readiness;
};

const StatusBadge = ({ status }: { status: ReadinessCheck["status"] }) => (
  <span
    className={`btn btn-sm ${
      status === "PASS" ? "btn-outline-success" : "btn-outline-danger"
    }`}
  >
    {status}
  </span>
);

const ReadinessContent = ({ readiness }: { readiness: Readiness }) => {
  const { runtime, checks, summary } = readiness;
  const allPass = summary.total > 0 && summary.pass === summary.total;
  const percent =
    summary.total > 0 ? Math.round((summary.pass / summary.total) * 100) : 0;

  return (
    <>
      <div className="row">
        <div className="col-md-6">
          <div className="card">
            <div className="card-block">
              <h4 className="card-title">Java</h4>
              <p className="card-text">
                {runtime.javaVersion} &rarr; {runtime.targetJavaVersion}
              </p>
            </div>
          </div>
        </div>
        <div className="col-md-6">
          <div className="card">
            <div className="card-block">
              <h4 className="card-title">Spring Boot</h4>
              <p className="card-text">
                {runtime.springBootVersion} &rarr;{" "}
                {runtime.targetSpringBootVersion}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="row">
        <div className="col-md-12">
          <p
            className={`summary-text ${
              allPass ? "text-success" : "text-danger"
            }`}
          >
            {summary.pass} / {summary.total} checks passing
          </p>
          <div className="progress">
            <div
              className={`progress-bar ${
                allPass ? "bg-success" : "bg-danger"
              }`}
              role="progressbar"
              style={{ width: `${percent}%` }}
              aria-valuenow={summary.pass}
              aria-valuemin={0}
              aria-valuemax={summary.total}
            />
          </div>
        </div>
      </div>

      <div className="row">
        <div className="col-md-12">
          <table className="table">
            <thead>
              <tr>
                <th>Check</th>
                <th>Status</th>
                <th>Detail</th>
              </tr>
            </thead>
            <tbody>
              {checks.map((check: ReadinessCheck) => (
                <tr key={check.id}>
                  <td>{check.title}</td>
                  <td>
                    <StatusBadge status={check.status} />
                  </td>
                  <td>{check.detail}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      <style jsx>
        {`
          .row {
            margin-bottom: 1.5rem;
          }
          .summary-text {
            font-weight: 600;
            margin-bottom: 0.5rem;
          }
          .progress {
            height: 1rem;
            background-color: #e0e0e0;
            border-radius: 0.25rem;
            overflow: hidden;
          }
          .text-success {
            color: #5cb85c;
          }
          .text-danger {
            color: #b85c5c;
          }
          .progress-bar {
            height: 100%;
            transition: width 0.3s ease;
          }
          .bg-success {
            background-color: #5cb85c;
          }
          .bg-danger {
            background-color: #b85c5c;
          }
        `}
      </style>
    </>
  );
};

const System = () => {
  const { data, error } = useSWR("/system/readiness", fetchReadiness);

  return (
    <>
      <Head>
        <title>SYSTEM | NEXT REALWORLD</title>
        <meta
          name="description"
          content="Migration readiness checks for the Java 17 / Spring Boot 3 upgrade"
        />
      </Head>
      <div className="system-page">
        <div className="container page">
          <h1>Migration Readiness</h1>
          {error ? (
            <ErrorMessage message="Cannot load migration readiness" />
          ) : !data ? (
            <LoadingSpinner />
          ) : (
            <ReadinessContent readiness={data} />
          )}
        </div>
      </div>
      <style jsx>
        {`
          h1 {
            margin-bottom: 1.5rem;
          }
        `}
      </style>
    </>
  );
};

export default System;
