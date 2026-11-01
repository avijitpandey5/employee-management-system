
import {
  createContext,
  useContext,
  useMemo,
  useState,
} from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [session, setSession] = useState(null);

  const login = (loginResponse) => {
    if (
      !loginResponse?.token ||
      !loginResponse?.username ||
      !loginResponse?.role
    ) {
      throw new Error("Invalid login response");
    }

    setSession({
      token: loginResponse.token,
      username: loginResponse.username,
      role: loginResponse.role,
      employeeId: loginResponse.employeeId,
    });
  };

  const logout = () => {
    setSession(null);
  };

  const value = useMemo(
    () => ({
      user: session
        ? {
            username: session.username,
            role: session.role,
            employeeId: session.employeeId,
          }
        : null,
      token: session?.token ?? null,
      isAuthenticated: Boolean(session?.token),
      login,
      logout,
    }),
    [session]
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}
