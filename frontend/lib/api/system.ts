import axios from "axios";

import readinessFixture from "./__fixtures__/readiness.json";
import { SERVER_BASE_URL } from "../utils/constant";

const SystemAPI = {
  getReadiness: () => {
    if (process.env.NEXT_PUBLIC_MOCK_READINESS === "1") {
      return Promise.resolve({ data: readinessFixture });
    }
    return axios.get(`${SERVER_BASE_URL}/system/readiness`);
  },
};
export default SystemAPI;
