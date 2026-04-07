import axios from "axios";

import { SERVER_BASE_URL } from "../utils/constant";

const ProjectAPI = {
  all: () => axios.get(`${SERVER_BASE_URL}/projects`),

  get: (id) => axios.get(`${SERVER_BASE_URL}/projects/${id}`),

  create: async (project, token) => {
    const { data, status } = await axios.post(
      `${SERVER_BASE_URL}/projects`,
      JSON.stringify({ project }),
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Token ${encodeURIComponent(token)}`,
        },
      }
    );
    return {
      data,
      status,
    };
  },

  update: async (id, project, token) => {
    const { data, status } = await axios.put(
      `${SERVER_BASE_URL}/projects/${id}`,
      JSON.stringify({ project }),
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Token ${encodeURIComponent(token)}`,
        },
      }
    );
    return {
      data,
      status,
    };
  },

  delete: (id, token) =>
    axios.delete(`${SERVER_BASE_URL}/projects/${id}`, {
      headers: {
        Authorization: `Token ${token}`,
      },
    }),
};

export default ProjectAPI;
