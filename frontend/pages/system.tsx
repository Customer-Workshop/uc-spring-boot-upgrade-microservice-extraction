import Head from "next/head";
import React from "react";
import useSWR from "swr";

import ErrorMessage from "../components/common/ErrorMessage";
import LoadingSpinner from "../components/common/LoadingSpinner";
import SystemAPI from "../lib/api/system";
import { ReadinessCheck, ReadinessResponse } from "../lib/types/readinessType";

const fetcher = async (): Promise<ReadinessResponse> => {
  const { data } = await SystemAPI.getReadiness();
  return data;
};

const StatusBadge = ({ status }: { status: ReadinessCheck["status"] }) => (
  <span
    className={`tag ${status === "PASS" ? "tag-success" : "tag-danger"}`}
  >
    {status}
  </span>
);

const System = () => {
  const { data, error } = useSWR("/system/readiness", fetcher);

  let content: React.ReactNode;
  if (error) {
    content = <ErrorMessage message="Cannot load migration readiness..." />;
  } else if (!data) {
    content = <LoadingSpinner />;
  } else {
    const { runtime, checks, summary } = data.readiness;
    const percent =
      summary.total > 0 ? Math.round((summary.pass / summary.total) * 100) : 0;
    content = (
      <>
        <div className="card m-b-2">
          <div className="card-block">
            <h1 className="card-title">Migration Readiness</h1>
            <table className="table table-sm m-b-0">
              <thead>
                <tr>
                  <th />
                  <th>Current</th>
                  <th>Target</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <th scope="row">Java</th>
                  <td>{runtime.javaVersion}</td>
                  <td>{runtime.targetJavaVersion}</td>
                </tr>
                <tr>
                  <th scope="row">Spring Boot</th>
                  <td>{runtime.springBootVersion}</td>
                  <td>{runtime.targetSpringBootVersion}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div className="m-b-2">
          <p className="m-b-0">
            <strong>
              {summary.pass} / {summary.total}
            </strong>{" "}
            checks passing
          </p>
          <progress
            className="progress progress-success"
            value={percent}
            max={100}
          >
            {percent}%
          </progress>
        </div>

        <table className="table">
          <thead>
            <tr>
              <th>Check</th>
              <th>Status</th>
              <th>Detail</th>
            </tr>
          </thead>
          <tbody>
            {checks.map((check) => (
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
      </>
    );
  }

  return (
    <>
      <Head>
        <title>SYSTEM | NEXT REALWORLD</title>
      </Head>
      <div className="system-page">
        <div className="container page">
          <div className="row">
            <div className="col-md-10 offset-md-1 col-xs-12">{content}</div>
          </div>
        </div>
      </div>
    </>
  );
};

export default System;
