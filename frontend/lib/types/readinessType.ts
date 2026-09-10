export type ReadinessStatus = "PASS" | "FAIL";

export interface ReadinessRuntime {
  javaVersion: string;
  springBootVersion: string;
  targetJavaVersion: string;
  targetSpringBootVersion: string;
}

export interface ReadinessCheck {
  id: string;
  title: string;
  status: ReadinessStatus;
  detail: string;
}

export interface ReadinessSummary {
  total: number;
  pass: number;
  fail: number;
}

export interface Readiness {
  runtime: ReadinessRuntime;
  checks: ReadinessCheck[];
  summary: ReadinessSummary;
}

export interface ReadinessResponse {
  readiness: Readiness;
}
