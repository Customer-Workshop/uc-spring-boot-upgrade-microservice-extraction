import Head from "next/head";
import React from "react";
import useSWR, { mutate } from "swr";

import ProjectAPI from "../../lib/api/project";
import { SERVER_BASE_URL } from "../../lib/utils/constant";
import checkLogin from "../../lib/utils/checkLogin";
import storage from "../../lib/utils/storage";

const STATUS_OPTIONS = ["active", "completed", "on-hold"];

const STATUS_COLORS = {
  active: "#5cb85c",
  completed: "#0275d8",
  "on-hold": "#f0ad4e",
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
  } = useSWR(`${SERVER_BASE_URL}/projects`, (url) =>
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
      const projectPayload = {
        name: form.name,
        description: form.description,
        client: form.client,
        status: form.status,
      };

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
      <div className="container page">
        <div className="row">
          <div className="col-md-12">
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: "20px",
                marginTop: "20px",
              }}
            >
              <h1>Projects</h1>
              {isLoggedIn && !showForm && (
                <button
                  className="btn btn-sm btn-outline-primary"
                  onClick={() => {
                    resetForm();
                    setShowForm(true);
                  }}
                >
                  <i className="ion-plus-round" /> New Project
                </button>
              )}
            </div>

            {showForm && isLoggedIn && (
              <div
                style={{
                  border: "1px solid #ddd",
                  borderRadius: "4px",
                  padding: "20px",
                  marginBottom: "20px",
                  backgroundColor: "#f8f8f8",
                }}
              >
                <h3>{editingId ? "Edit Project" : "New Project"}</h3>
                {formErrors.length > 0 && (
                  <ul className="error-messages">
                    {formErrors.map((err, i) => (
                      <li key={i}>{err}</li>
                    ))}
                  </ul>
                )}
                <form onSubmit={handleSubmit}>
                  <fieldset disabled={isSubmitting}>
                    <fieldset className="form-group">
                      <input
                        className="form-control form-control-lg"
                        type="text"
                        placeholder="Project Name *"
                        name="name"
                        value={form.name}
                        onChange={handleChange}
                      />
                    </fieldset>
                    <fieldset className="form-group">
                      <textarea
                        className="form-control"
                        rows={4}
                        placeholder="Description"
                        name="description"
                        value={form.description}
                        onChange={handleChange}
                      />
                    </fieldset>
                    <fieldset className="form-group">
                      <input
                        className="form-control"
                        type="text"
                        placeholder="Client"
                        name="client"
                        value={form.client}
                        onChange={handleChange}
                      />
                    </fieldset>
                    <fieldset className="form-group">
                      <label style={{ marginRight: "10px", fontWeight: "bold" }}>
                        Start Date:
                      </label>
                      <input
                        className="form-control"
                        type="date"
                        name="startDate"
                        value={form.startDate}
                        onChange={handleChange}
                        style={{ display: "inline-block", width: "auto" }}
                      />
                    </fieldset>
                    <fieldset className="form-group">
                      <label style={{ marginRight: "10px", fontWeight: "bold" }}>
                        Status: *
                      </label>
                      <select
                        className="form-control"
                        name="status"
                        value={form.status}
                        onChange={handleChange}
                        style={{ display: "inline-block", width: "auto" }}
                      >
                        {STATUS_OPTIONS.map((s) => (
                          <option key={s} value={s}>
                            {s}
                          </option>
                        ))}
                      </select>
                    </fieldset>
                    <button
                      className="btn btn-lg btn-primary pull-xs-right"
                      type="submit"
                    >
                      {editingId ? "Update Project" : "Create Project"}
                    </button>
                    <button
                      className="btn btn-lg btn-secondary pull-xs-right"
                      type="button"
                      onClick={resetForm}
                      style={{ marginRight: "10px" }}
                    >
                      Cancel
                    </button>
                  </fieldset>
                </form>
              </div>
            )}

            {error && <div className="error-messages">Failed to load projects.</div>}

            {!error && !projectsData && <div>Loading projects...</div>}

            {projects.length === 0 && projectsData && (
              <div style={{ textAlign: "center", padding: "40px", color: "#999" }}>
                No projects yet. Create your first project!
              </div>
            )}

            {projects.length > 0 && (
              <div>
                {projects.map((project) => (
                  <div
                    key={project.id}
                    style={{
                      border: "1px solid #ddd",
                      borderRadius: "4px",
                      padding: "20px",
                      marginBottom: "15px",
                      backgroundColor: "#fff",
                    }}
                  >
                    <div
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "flex-start",
                      }}
                    >
                      <div style={{ flex: 1 }}>
                        <h3 style={{ marginBottom: "5px" }}>{project.name}</h3>
                        <span
                          style={{
                            display: "inline-block",
                            padding: "2px 10px",
                            borderRadius: "12px",
                            backgroundColor:
                              STATUS_COLORS[project.status] || "#999",
                            color: "#fff",
                            fontSize: "12px",
                            marginBottom: "10px",
                          }}
                        >
                          {project.status}
                        </span>
                        {project.description && (
                          <p style={{ color: "#666", marginTop: "8px" }}>
                            {project.description}
                          </p>
                        )}
                        <div style={{ color: "#999", fontSize: "14px" }}>
                          {project.client && (
                            <span style={{ marginRight: "20px" }}>
                              <strong>Client:</strong> {project.client}
                            </span>
                          )}
                          {project.startDate && (
                            <span style={{ marginRight: "20px" }}>
                              <strong>Start:</strong>{" "}
                              {new Date(project.startDate).toLocaleDateString()}
                            </span>
                          )}
                          {project.createdAt && (
                            <span>
                              <strong>Created:</strong>{" "}
                              {new Date(project.createdAt).toLocaleDateString()}
                            </span>
                          )}
                        </div>
                      </div>
                      {isLoggedIn && (
                        <div
                          style={{
                            display: "flex",
                            gap: "8px",
                            marginLeft: "15px",
                          }}
                        >
                          <button
                            className="btn btn-sm btn-outline-secondary"
                            onClick={() => handleEdit(project)}
                          >
                            <i className="ion-edit" /> Edit
                          </button>
                          <button
                            className="btn btn-sm btn-outline-danger"
                            onClick={() => handleDelete(project.id)}
                          >
                            <i className="ion-trash-a" /> Delete
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default Projects;
