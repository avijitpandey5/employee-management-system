
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowRight,
  Bell,
  Building2,
  ChevronLeft,
  ChevronRight,
  FileText,
  LayoutDashboard,
  LogOut,
  Menu,
  UserRound,
  CalendarDays,
  X,
} from "lucide-react";

import { useAuth } from "../../context/AuthContext";

const navigation = [
  {
    label: "Dashboard",
    icon: LayoutDashboard,
    path: "/employee/dashboard",
  },
  {
    label: "My Profile",
    icon: UserRound,
    path: "/employee/profile",
  },
  {
    label: "My Leaves",
    icon: CalendarDays,
    path: "/employee/leaves",
  },
  {
    label: "My Documents",
    icon: FileText,
    path: "/employee/documents",
  },
];

export default function EmployeeDashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  const handleNavigation = (path) => {
  setMobileOpen(false);

  if (
    path === "/employee/dashboard" ||
    path === "/employee/profile"
  ) {
    navigate(path);
  }
};

  return (
    <div className="min-h-screen bg-[#f7f8fc] text-slate-900">

      {/* Mobile backdrop */}
      {mobileOpen && (
        <button
          type="button"
          aria-label="Close navigation"
          onClick={() => setMobileOpen(false)}
          className="fixed inset-0 z-40 bg-slate-950/40 lg:hidden"
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 flex flex-col border-r border-slate-200 bg-white transition-all duration-300
          ${collapsed ? "lg:w-20" : "lg:w-64"}
          ${mobileOpen ? "translate-x-0" : "-translate-x-full"}
          w-64 lg:translate-x-0`}
      >
        <div className="flex h-20 items-center justify-between border-b border-slate-100 px-5">
          <div className="flex items-center gap-3 overflow-hidden">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-indigo-600 text-white">
              <Building2 size={21} />
            </div>

            {!collapsed && (
              <span className="text-xl font-bold tracking-tight">
                PeopleFlow
              </span>
            )}
          </div>

          <button
            type="button"
            aria-label="Close sidebar"
            onClick={() => setMobileOpen(false)}
            className="rounded-lg p-1 text-slate-500 lg:hidden"
          >
            <X size={20} />
          </button>
        </div>

        <div className="px-3 pt-7">
          {!collapsed && (
            <p className="mb-3 px-3 text-xs font-semibold uppercase tracking-widest text-slate-400">
              Workspace
            </p>
          )}

          <nav className="space-y-1">
            {navigation.map((item) => {
              const Icon = item.icon;
              const active = item.path === "/employee/dashboard";

              return (
                <button
                  key={item.path}
                  type="button"
                  title={item.label}
                  onClick={() => handleNavigation(item.path)}
                  className={`flex w-full items-center gap-3 rounded-xl px-3 py-3 text-left text-sm font-medium transition-colors
                    ${
                      active
                        ? "bg-indigo-50 text-indigo-700"
                        : "text-slate-600 hover:bg-slate-50 hover:text-slate-900"
                    }`}
                >
                  <Icon size={19} className="shrink-0" />

                  {!collapsed && <span>{item.label}</span>}
                </button>
              );
            })}
          </nav>
        </div>

        <div className="mt-auto border-t border-slate-100 p-3">
          <button
            type="button"
            onClick={() => setCollapsed((value) => !value)}
            className="hidden w-full items-center gap-3 rounded-xl px-3 py-3 text-sm text-slate-500 hover:bg-slate-50 lg:flex"
          >
            {collapsed ? (
              <ChevronRight size={19} />
            ) : (
              <>
                <ChevronLeft size={19} />
                Collapse sidebar
              </>
            )}
          </button>

          <button
            type="button"
            onClick={handleLogout}
            className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm font-medium text-red-600 hover:bg-red-50"
          >
            <LogOut size={19} />
            {!collapsed && "Logout"}
          </button>
        </div>
      </aside>

      {/* Main content */}
      <div
        className={`min-h-screen transition-all duration-300 ${
          collapsed ? "lg:pl-20" : "lg:pl-64"
        }`}
      >
        {/* Header */}
        <header className="sticky top-0 z-30 flex h-20 items-center justify-between border-b border-slate-200 bg-white/90 px-5 backdrop-blur-xl sm:px-8">
          <div className="flex items-center gap-4">
            <button
              type="button"
              aria-label="Open navigation"
              onClick={() => setMobileOpen(true)}
              className="rounded-lg p-2 text-slate-600 hover:bg-slate-100 lg:hidden"
            >
              <Menu size={22} />
            </button>

            <div>
              <p className="text-xs text-slate-400">
                Employee workspace
              </p>
              <h2 className="text-lg font-semibold">
                Dashboard
              </h2>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div
              title="Notifications will be available in a future update"
              className="rounded-xl p-2 text-slate-400"
            >
              <Bell size={20} />
            </div>

            <div className="hidden text-right sm:block">
              <p className="text-sm font-semibold">
                {user?.username}
              </p>
              <p className="text-xs text-slate-400">
                Employee
              </p>
            </div>

            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-indigo-100 font-semibold text-indigo-700">
              {user?.username?.charAt(0)?.toUpperCase() || "E"}
            </div>
          </div>
        </header>

        {/* Dashboard content */}
        <main className="mx-auto max-w-7xl space-y-8 px-5 py-8 sm:px-8">

          {/* Welcome section */}
          <section className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-indigo-600 via-indigo-600 to-violet-600 p-8 text-white shadow-lg shadow-indigo-500/10 sm:p-10">
            <div className="pointer-events-none absolute -right-20 -top-28 h-80 w-80 rounded-full bg-white/10 blur-3xl" />

            <div className="relative z-10 max-w-2xl">
              <p className="mb-3 text-sm font-medium text-indigo-100">
                Your workspace
              </p>

              <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
                Welcome back, {user?.username}!
              </h1>

              <p className="mt-4 max-w-xl leading-7 text-indigo-100">
                Everything you need for your workday, organized
                in one beautiful workspace.
              </p>

              <div className="mt-6 inline-flex items-center gap-2 rounded-full border border-white/20 bg-white/10 px-4 py-2 text-sm">
                <UserRound size={16} />
                Employee ID: {user?.employeeId ?? "Not assigned"}
              </div>
            </div>
          </section>

          {/* Section heading */}
          <section>
            <div className="mb-5">
              <h2 className="text-xl font-bold tracking-tight">
                Quick access
              </h2>

              <p className="mt-1 text-sm text-slate-500">
                Your everyday employee tools.
              </p>
            </div>

            <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
              {[
                {
                  title: "My Profile",
                  description:
                    "View your employee information and department details.",
                  icon: UserRound,
                  color: "bg-indigo-50 text-indigo-600",
                  path: "/employee/profile",
                },
                {
                  title: "Leave Management",
                  description:
                    "Apply for leave and track your request status.",
                  icon: CalendarDays,
                  color: "bg-violet-50 text-violet-600",
                  path: "/employee/leaves",
                },
                {
                  title: "My Documents",
                  description:
                    "Access and manage your employee documents.",
                  icon: FileText,
                  color: "bg-sky-50 text-sky-600",
                  path: "/employee/documents",
                },
              ].map((item) => {
                const Icon = item.icon;

                return (
                  <div
                    key={item.title}
                    className="group rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-lg"
                  >
                    <div
                      className={`mb-5 flex h-12 w-12 items-center justify-center rounded-xl ${item.color}`}
                    >
                      <Icon size={23} />
                    </div>

                    <h3 className="text-lg font-bold">
                      {item.title}
                    </h3>

                    <p className="mt-2 min-h-12 text-sm leading-6 text-slate-500">
                      {item.description}
                    </p>

                    <span className="mt-5 inline-flex items-center gap-2 text-sm font-semibold text-indigo-600">
                      Coming next
                      <ArrowRight size={16} />
                    </span>
                  </div>
                );
              })}
            </div>
          </section>

          {/* Information section */}
          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-bold">
              Your employee workspace
            </h2>

            <p className="mt-3 text-sm leading-7 text-slate-500">
              Your account is connected to the PeopleFlow
              authentication system. In the next stage,
              we'll connect employee details, leave requests,
              and documents directly to your Spring Boot APIs.
            </p>
          </section>

        </main>
      </div>
    </div>
  );
}
