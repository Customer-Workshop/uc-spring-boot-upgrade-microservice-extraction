import axios from "axios";

import readinessFixture from "./__fixtures__/readiness.json";
import { ReadinessResponse } from "../types/readinessType";
import { SERVER_BASE_URL } from "../utils/constant";

const SystemAPI = {
  getReadiness: () => {
    if (process.env.NEXT_PUBLIC_MOCK_READINESS === "1") {
      return Promise.resolve({ data: readinessFixture as ReadinessResponse });
    }
    return axios.get<ReadinessResponse>(`${SERVER_BASE_URL}/system/readiness`);
  },
};

export default SystemAPI;
