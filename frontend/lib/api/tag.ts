import axios from "axios";

import { SERVER_BASE_URL } from "../utils/constant";

const TagAPI = {
  getAll: () => axios.get(`${SERVER_BASE_URL}/tags`),
  getStats: () => axios.get(`${SERVER_BASE_URL}/tags/stats`),
};
export default TagAPI;
