import axios from "axios";

import { ReadinessResponse } from "../types/readinessType";
import { SERVER_BASE_URL } from "../utils/constant";

const SystemAPI = {
  getReadiness: () =>
    axios.get<ReadinessResponse>(`${SERVER_BASE_URL}/system/readiness`),
};

export default SystemAPI;
