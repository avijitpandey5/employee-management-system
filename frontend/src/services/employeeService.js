
import api from "./api";

export async function getMyProfile(token) {
  const response = await api.get("/api/employees/me", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.data;
}
