import Head from "next/head";
import React from "react";
import useSWR, { mutate } from "swr";

import ProjectAPI from "../../lib/api/project";
import { SERVER_BASE_URL } from "../../lib/utils/constant";
import checkLogin from "../../lib/utils/checkLogin";
import storage from "../../lib/utils/storage";

const STATUS_OPTIONS = ["active", "completed", "on-hold"];

const STATUS_STYLES = {
  active: "bg-green-100 text-green-800 border border-green-200",
  completed: "bg-blue-100 text-blue-800 border border-blue-200",
  "on-hold": "bg-amber-100 text-amber-800 border border-amber-200",
};

const initialFormState = {
  name: "",
  description: "",
  client: "",
  startDate: "",
  status: "active",
};

const Projects = () => {
  const { data: currentUser } = useSWR("user", storage);
  const isLoggedIn = checkLogin(currentUser);
  const {
    data: projectsData,
    error,
  } = useSWR(`${SERVER_BASE_URL}/projects`, () =>
    ProjectAPI.all().then((res) => res.data)
  );

  const [showForm, setShowForm] = React.useState(false);
  const [editingId, setEditingId] = React.useState(null);
  const [form, setForm] = React.useState(initialFormState);
  const [formErrors, setFormErrors] = React.useState([]);
  const [isSubmitting, setIsSubmitting] = React.useState(false);

  const projects = projectsData?.projects || [];

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const resetForm = () => {
    setForm(initialFormState);
    setEditingId(null);
    setShowForm(false);
    setFormErrors([]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setFormErrors([]);

    const errors = [];
    if (!form.name.trim()) errors.push("Name is required");
    if (!form.status) errors.push("Status is required");

    if (errors.length > 0) {
      setFormErrors(errors);
      setIsSubmitting(false);
      return;
    }

    try {
      const projectPayload: any = {
        name: form.name,
        description: form.description,
        client: form.client,
        status: form.status,
      };
      if (form.startDate) {
        projectPayload.startDate = new Date(form.startDate).toISOString();
      }

      if (editingId) {
        await ProjectAPI.update(editingId, projectPayload, currentUser?.token);
      } else {
        await ProjectAPI.create(projectPayload, currentUser?.token);
      }
      mutate(`${SERVER_BASE_URL}/projects`);
      resetForm();
    } catch (err) {
      if (err.response?.data?.errors) {
        const apiErrors = err.response.data.errors;
        const errorMessages = Object.keys(apiErrors).map(
          (key) => `${key} ${apiErrors[key].join(", ")}`
        );
        setFormErrors(errorMessages);
      } else {
        setFormErrors(["An error occurred. Please try again."]);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEdit = (project) => {
    setForm({
      name: project.name || "",
      description: project.description || "",
      client: project.client || "",
      startDate: project.startDate
        ? new Date(project.startDate).toISOString().split("T")[0]
        : "",
      status: project.status || "active",
    });
    setEditingId(project.id);
    setShowForm(true);
    setFormErrors([]);
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this project?")) {
      return;
    }
    try {
      await ProjectAPI.delete(id, currentUser?.token);
      mutate(`${SERVER_BASE_URL}/projects`);
    } catch (err) {
      alert("Failed to delete project.");
    }
  };

  return (
    <>
      <Head>
        <title>Projects | NEXT REALWORLD</title>
      </Head>
      <div className="tw">
        <div style={{ maxWidth: "960px", margin: "0 auto", padding: "32px 16px", fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif' }}>
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <div>
              <h1 className="text-3xl font-bold tracking-tight text-foreground">Projects</h1>
              <p className="text-sm text-muted-foreground mt-1">Manage your projects and track their progress.</p>
            </div>
            {isLoggedIn && !showForm && (
              <button
                onClick={() => { resetForm(); setShowForm(true); }}
                className="inline-flex items-center justify-center rounded-md text-sm font-medium bg-primary text-primary-foreground h-10 px-4 py-2 hover:bg-opacity-90 transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
              >
                <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
                New Project
              </button>
            )}
          </div>

          {/* Form Card */}
          {showForm && isLoggedIn && (
            <div className="rounded-lg border border-border bg-card shadow-sm mb-8">
              <div className="p-6">
                <h3 className="text-lg font-semibold text-foreground mb-1">
                  {editingId ? "Edit Project" : "Create New Project"}
                </h3>
                <p className="text-sm text-muted-foreground mb-6">
                  {editingId ? "Update the project details below." : "Fill in the details to create a new project."}
                </p>

                {formErrors.length > 0 && (
                  <div className="rounded-md bg-red-50 border border-red-200 p-4 mb-6">
                    <div className="flex">
                      <svg className="h-5 w-5 text-red-400 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <div>
                        {formErrors.map((err, i) => (
                          <p key={i} className="text-sm text-red-700">{err}</p>
                        ))}
                      </div>
                    </div>
                  </div>
                )}

                <form onSubmit={handleSubmit}>
                  <fieldset disabled={isSubmitting}>
                    <div className="space-y-4">
                      <div>
                        <label className="text-sm font-medium text-foreground block mb-1.5">
                          Project Name <span className="text-red-500">*</span>
                        </label>
                        <input
                          className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:opacity-50"
                          type="text" placeholder="Enter project name" name="name" value={form.name} onChange={handleChange}
                        />
                      </div>
                      <div>
                        <label className="text-sm font-medium text-foreground block mb-1.5">Description</label>
                        <textarea
                          className="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:opacity-50"
                          rows={3} placeholder="Describe the project..." name="description" value={form.description} onChange={handleChange}
                          style={{ minHeight: "80px", resize: "vertical" }}
                        />
                      </div>
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div>
                          <label className="text-sm font-medium text-foreground block mb-1.5">Client</label>
                          <input
                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:opacity-50"
                            type="text" placeholder="Client name" name="client" value={form.client} onChange={handleChange}
                          />
                        </div>
                        <div>
                          <label className="text-sm font-medium text-foreground block mb-1.5">Start Date</label>
                          <input
                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:opacity-50"
                            type="date" name="startDate" value={form.startDate} onChange={handleChange}
                          />
                        </div>
                      </div>
                      <div>
                        <label className="text-sm font-medium text-foreground block mb-1.5">
                          Status <span className="text-red-500">*</span>
                        </label>
                        <select
                          className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:opacity-50"
                          name="status" value={form.status} onChange={handleChange}
                        >
                          {STATUS_OPTIONS.map((s) => (
                            <option key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</option>
                          ))}
                        </select>
                      </div>
                    </div>
                    <div className="flex items-center justify-end gap-3 mt-6 pt-6 border-t border-border">
                      <button type="button" onClick={resetForm}
                        className="inline-flex items-center justify-center rounded-md text-sm font-medium border border-input bg-background text-foreground h-10 px-4 py-2 hover:bg-accent hover:text-accent-foreground transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
                      >Cancel</button>
                      <button type="submit"
                        className="inline-flex items-center justify-center rounded-md text-sm font-medium bg-primary text-primary-foreground h-10 px-4 py-2 hover:bg-opacity-90 transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:opacity-50"
                      >
                        {isSubmitting ? "Saving..." : editingId ? "Save Changes" : "Create Project"}
                      </button>
                    </div>
                  </fieldset>
                </form>
              </div>
            </div>
          )}

          {/* Error State */}
          {error && (
            <div className="rounded-lg border border-red-200 bg-red-50 p-6 text-center">
              <svg className="mx-auto h-10 w-10 text-red-400 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p className="text-sm font-medium text-red-800">Failed to load projects.</p>
            </div>
          )}

          {/* Loading State */}
          {!error && !projectsData && (
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <div key={i} className="rounded-lg border border-border p-6 animate-pulse">
                  <div className="h-5 bg-gray-200 rounded w-1/3 mb-3" />
                  <div className="h-4 bg-gray-200 rounded w-1/5 mb-4" />
                  <div className="h-4 bg-gray-100 rounded w-2/3" />
                </div>
              ))}
            </div>
          )}

          {/* Empty State */}
          {projects.length === 0 && projectsData && (
            <div className="rounded-lg border-2 border-dashed border-border p-12 text-center">
              <svg className="mx-auto h-12 w-12 text-muted-foreground mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
              </svg>
              <h3 className="text-lg font-medium text-foreground mb-1">No projects yet</h3>
              <p className="text-sm text-muted-foreground">Get started by creating your first project.</p>
            </div>
          )}

          {/* Project Cards */}
          {projects.length > 0 && (
            <div className="space-y-3">
              {projects.map((project) => (
                <div key={project.id} className="group rounded-lg border border-border bg-card p-5 hover:shadow-md transition-shadow duration-200">
                  <div className="flex items-start justify-between">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-3 mb-2">
                        <h3 className="text-base font-semibold text-foreground truncate">{project.name}</h3>
                        <span className={"inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium " + (STATUS_STYLES[project.status] || "bg-gray-100 text-gray-800 border border-gray-200")}>
                          {project.status}
                        </span>
                      </div>
                      {project.description && (
                        <p className="text-sm text-muted-foreground mb-3">{project.description}</p>
                      )}
                      <div className="flex flex-wrap items-center gap-4 text-xs text-muted-foreground">
                        {project.client && (
                          <span className="inline-flex items-center gap-1.5">
                            <svg className="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                            </svg>
                            {project.client}
                          </span>
                        )}
                        {project.startDate && (
                          <span className="inline-flex items-center gap-1.5">
                            <svg className="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                            {new Date(project.startDate).toLocaleDateString("en-US", { year: "numeric", month: "short", day: "numeric" })}
                          </span>
                        )}
                        {project.createdAt && (
                          <span className="inline-flex items-center gap-1.5">
                            <svg className="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                            {"Created " + new Date(project.createdAt).toLocaleDateString("en-US", { year: "numeric", month: "short", day: "numeric" })}
                          </span>
                        )}
                      </div>
                    </div>
                    {isLoggedIn && (
                      <div className="flex items-center gap-1 ml-4 opacity-0 group-hover:opacity-100 transition-opacity">
                        <button onClick={() => handleEdit(project)} title="Edit"
                          className="inline-flex items-center justify-center rounded-md h-8 w-8 text-muted-foreground hover:text-foreground hover:bg-accent transition-colors focus:outline-none"
                        >
                          <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                          </svg>
                        </button>
                        <button onClick={() => handleDelete(project.id)} title="Delete"
                          className="inline-flex items-center justify-center rounded-md h-8 w-8 text-muted-foreground hover:text-destructive hover:bg-red-50 transition-colors focus:outline-none"
                        >
                          <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                          </svg>
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Project count */}
          {projects.length > 0 && (
            <div className="mt-4 text-xs text-muted-foreground">
              {projects.length} project{projects.length !== 1 ? "s" : ""}
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default Projects;
