import axios from "axios";

import { SERVER_BASE_URL } from "../utils/constant";

const SystemAPI = {
  getReadiness: () => axios.get(`${SERVER_BASE_URL}/system/readiness`),
};
export default SystemAPI;
