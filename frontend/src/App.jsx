
import EmployeeDashboard from "./pages/employee/EmployeeDashboard";
import EmployeeProfile from "./pages/employee/EmployeeProfile";

import {
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import LoginPage from "./pages/auth/LoginPage";
import { useAuth } from "./context/AuthContext";

function DashboardPlaceholder({ title }) {
  const { user, logout } = useAuth();

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 px-6">
      <div className="w-full max-w-lg rounded-2xl border border-slate-200 bg-white p-8 shadow-xl">
        <h1 className="text-3xl font-bold text-slate-900">
          {title}
        </h1>

        <p className="mt-4 text-slate-600">
          Welcome, {user?.username}!
        </p>

        <p className="mt-2 text-sm text-slate-500">
          Authentication successful. Your dashboard UI will
          be built next.
        </p>

        <button
          onClick={logout}
          className="mt-6 rounded-xl bg-indigo-600 px-5 py-3 font-semibold text-white transition hover:bg-indigo-700"
        >
          Logout
        </button>
      </div>
    </main>
  );
}

function ProtectedRoute({ role, children }) {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user.role !== role) {
    const homePaths = {
      ADMIN: "/admin/dashboard",
      MANAGER: "/manager/dashboard",
      EMPLOYEE: "/employee/dashboard",
    };

    return (
      <Navigate
        to={homePaths[user.role] || "/login"}
        replace
      />
    );
  }

  return children;
}

function App() {
  const { isAuthenticated, user } = useAuth();

  const homePaths = {
    ADMIN: "/admin/dashboard",
    MANAGER: "/manager/dashboard",
    EMPLOYEE: "/employee/dashboard",
  };

  const home = isAuthenticated
    ? homePaths[user?.role] || "/login"
    : "/login";

  return (
    <Routes>
      <Route
        path="/"
        element={<Navigate to={home} replace />}
      />

      <Route
        path="/login"
        element={
          isAuthenticated
            ? <Navigate to={home} replace />
            : <LoginPage />
        }
      />

      <Route
        path="/admin/dashboard"
        element={
          <ProtectedRoute role="ADMIN">
            <DashboardPlaceholder title="Admin Dashboard" />
          </ProtectedRoute>
        }
      />

      <Route
        path="/manager/dashboard"
        element={
          <ProtectedRoute role="MANAGER">
            <DashboardPlaceholder title="Manager Dashboard" />
          </ProtectedRoute>
        }
      />

      <Route
  path="/employee/dashboard"
  element={
    <ProtectedRoute role="EMPLOYEE">
      <EmployeeDashboard />
    </ProtectedRoute>
  }
/>

      <Route
        path="*"
        element={<Navigate to={home} replace />}
      />

      <Route
        path="/employee/profile"
        element={
         <ProtectedRoute role="EMPLOYEE">
         <EmployeeProfile />
         </ProtectedRoute>
      }
     />

    </Routes>
  );
}

export default App;
