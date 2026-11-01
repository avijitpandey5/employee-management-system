
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser } from "../../services/authService";
import { useAuth } from "../../context/AuthContext";

import {
  ArrowRight,
  ArrowUpRight,
  Building2,
  CheckCircle2,
  Eye,
  EyeOff,
  LockKeyhole,
  Mail,
  ShieldCheck,
  Sparkles,
  Users,
} from "lucide-react";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [form, setForm] = useState({
    username: "",
    password: "",
  });

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));

    if (error) setError("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (loading) return;

    setError("");
    setLoading(true);

    try {
      const response = await loginUser(
        form.username.trim(),
        form.password
      );

      if (
        !response?.token ||
        !response?.username ||
        !["ADMIN", "MANAGER", "EMPLOYEE"].includes(response.role)
      ) {
        throw new Error("Invalid login response");
      }

      login(response);

      const dashboardRoutes = {
        ADMIN: "/admin/dashboard",
        MANAGER: "/manager/dashboard",
        EMPLOYEE: "/employee/dashboard",
      };

      navigate(dashboardRoutes[response.role], {
        replace: true,
      });
    } catch (err) {
      if (err.response?.status === 401) {
        setError("Invalid username or password. Please try again.");
      } else if (err.response?.status === 403) {
        setError("Your account is not authorized to sign in.");
      } else if (err.response) {
        setError("Sign in failed. Please try again.");
      } else if (err.message === "Invalid login response") {
        setError("The server returned an invalid login response.");
      } else {
        setError("Unable to connect to the server. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen bg-[#f8fafc] lg:grid lg:grid-cols-2">

      {/* LEFT BRAND PANEL */}
      <section className="relative hidden min-h-screen overflow-hidden bg-[#10152e] p-12 text-white lg:flex lg:flex-col lg:justify-between xl:p-16">

        <div className="pointer-events-none absolute -left-32 top-1/4 h-96 w-96 rounded-full bg-indigo-500/25 blur-[110px]" />

        <div className="pointer-events-none absolute -right-24 bottom-0 h-96 w-96 rounded-full bg-violet-500/25 blur-[110px]" />

        <div className="relative z-10 flex items-center gap-3">
          <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-white/15 ring-1 ring-white/20">
            <Building2 size={23} />
          </div>

          <span className="text-xl font-bold tracking-tight">
            PeopleFlow
          </span>
        </div>

        <div className="relative z-10 max-w-xl">

          <div className="mb-7 inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/10 px-4 py-2 text-sm text-indigo-100 backdrop-blur">
            <Sparkles size={15} />
            The modern employee workspace
          </div>

          <h1 className="text-5xl font-bold leading-[1.12] tracking-tight xl:text-6xl">
            Great teams deserve
            <span className="block bg-gradient-to-r from-indigo-300 via-purple-300 to-pink-300 bg-clip-text text-transparent">
              better tools.
            </span>
          </h1>

          <p className="mt-7 max-w-lg text-lg leading-8 text-slate-300">
            One beautiful workspace to manage your people,
            streamline everyday operations, and help your
            organization thrive.
          </p>

          <div className="mt-10 grid grid-cols-2 gap-4">

            <div className="rounded-2xl border border-white/10 bg-white/5 p-5 backdrop-blur">
              <Users
                className="mb-4 text-indigo-300"
                size={25}
              />

              <h3 className="font-semibold">
                People first
              </h3>

              <p className="mt-2 text-sm leading-6 text-slate-400">
                Employee information and teamwork in one place.
              </p>
            </div>

            <div className="rounded-2xl border border-white/10 bg-white/5 p-5 backdrop-blur">
              <ShieldCheck
                className="mb-4 text-violet-300"
                size={25}
              />

              <h3 className="font-semibold">
                Role-based access
              </h3>

              <p className="mt-2 text-sm leading-6 text-slate-400">
                Dedicated experiences for your entire team.
              </p>
            </div>

          </div>
        </div>

        <div className="relative z-10 flex items-center justify-between text-sm text-slate-400">
          <span>
            © {new Date().getFullYear()} PeopleFlow
          </span>

          <span className="flex items-center gap-1">
            Designed for modern teams
            <ArrowUpRight size={14} />
          </span>
        </div>

      </section>

      {/* RIGHT LOGIN PANEL */}
      <section className="flex min-h-screen flex-col px-6 py-8 sm:px-10 lg:px-16">

        {/* Mobile branding */}
        <div className="flex items-center gap-3 lg:hidden">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-600 text-white">
            <Building2 size={21} />
          </div>

          <span className="text-xl font-bold tracking-tight text-slate-900">
            PeopleFlow
          </span>
        </div>

        <div className="flex flex-1 items-center justify-center py-12">

          <div className="w-full max-w-[420px]">

            <div className="mb-9">
              <div className="mb-6 inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600">
                <LockKeyhole size={25} />
              </div>

              <h2 className="text-3xl font-bold tracking-tight text-slate-950 sm:text-4xl">
                Welcome back
              </h2>

              <p className="mt-3 text-base text-slate-500">
                Enter your credentials to access your workspace.
              </p>
            </div>

            {/* LOGIN FORM */}
            <form
              onSubmit={handleSubmit}
              className="space-y-5"
            >

              {/* Username */}
              <div>
                <label
                  htmlFor="username"
                  className="mb-2 block text-sm font-semibold text-slate-700"
                >
                  Username
                </label>

                <div className="relative">
                  <Mail
                    size={19}
                    className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
                  />

                  <input
                    id="username"
                    name="username"
                    type="text"
                    autoComplete="username"
                    required
                    maxLength={100}
                    disabled={loading}
                    value={form.username}
                    onChange={handleChange}
                    placeholder="Enter your username"
                    className="h-13 w-full rounded-xl border border-slate-200 bg-white pl-12 pr-4 text-sm text-slate-900 shadow-sm outline-none transition-all placeholder:text-slate-400 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 disabled:opacity-60"
                  />
                </div>
              </div>

              {/* Password */}
              <div>
                <label
                  htmlFor="password"
                  className="mb-2 block text-sm font-semibold text-slate-700"
                >
                  Password
                </label>

                <div className="relative">
                  <LockKeyhole
                    size={19}
                    className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
                  />

                  <input
                    id="password"
                    name="password"
                    type={showPassword ? "text" : "password"}
                    autoComplete="current-password"
                    required
                    disabled={loading}
                    value={form.password}
                    onChange={handleChange}
                    placeholder="Enter your password"
                    className="h-13 w-full rounded-xl border border-slate-200 bg-white pl-12 pr-12 text-sm text-slate-900 shadow-sm outline-none transition-all placeholder:text-slate-400 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 disabled:opacity-60"
                  />

                  <button
                    type="button"
                    onClick={() =>
                      setShowPassword((previous) => !previous)
                    }
                    aria-label={
                      showPassword
                        ? "Hide password"
                        : "Show password"
                    }
                    aria-pressed={showPassword}
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 transition hover:text-slate-700"
                  >
                    {showPassword ? (
                      <EyeOff size={19} />
                    ) : (
                      <Eye size={19} />
                    )}
                  </button>
                </div>
              </div>

              {/* Login error */}
              {error && (
                <div
                  role="alert"
                  className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm leading-6 text-red-700"
                >
                  {error}
                </div>
              )}

              {/* Sign in */}
              <button
                type="submit"
                disabled={loading}
                className="group flex h-13 w-full items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-indigo-600 to-violet-600 font-semibold text-white shadow-lg shadow-indigo-500/20 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-xl hover:shadow-indigo-500/30 active:translate-y-0 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:translate-y-0"
              >
                {loading ? (
                  <>
                    <span
                      aria-hidden="true"
                      className="h-5 w-5 animate-spin rounded-full border-2 border-white/30 border-t-white"
                    />
                    Signing in...
                  </>
                ) : (
                  <>
                    Sign in to workspace
                    <ArrowRight
                      size={18}
                      className="transition-transform group-hover:translate-x-1"
                    />
                  </>
                )}
              </button>

            </form>

            <div className="mt-8 flex items-center justify-center gap-2 text-sm text-slate-500">
              <CheckCircle2
                size={16}
                className="text-emerald-500"
              />
              Secure employee access
            </div>

            <p className="mt-10 text-center text-sm text-slate-500">
              Need access to your workspace?{" "}
              <span className="font-semibold text-indigo-600">
                Contact your administrator
              </span>
            </p>

          </div>
        </div>

        <footer className="text-center text-xs text-slate-400">
          © {new Date().getFullYear()} PeopleFlow.
          All rights reserved.
        </footer>

      </section>
    </main>
  );
}
