
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  BriefcaseBusiness,
  Building2,
  CalendarDays,
  Hash,
  Mail,
  Phone,
  UserRound,
  Users,
} from "lucide-react";

import { useAuth } from "../../context/AuthContext";
import { getMyProfile } from "../../services/employeeService";

function ProfileField({ label, value, icon: Icon }) {
  return (
    <div className="flex gap-3 py-4">
      <div className="mt-0.5 text-slate-400">
        <Icon size={18} />
      </div>

      <div className="min-w-0">
        <p className="text-xs font-medium text-slate-500">
          {label}
        </p>
        <p className="mt-1 break-words text-sm font-medium text-slate-900">
          {value || "Not provided"}
        </p>
      </div>
    </div>
  );
}

function formatDate(date) {
  if (!date) return "Not provided";

  const parsed = new Date(`${date}T00:00:00`);

  if (Number.isNaN(parsed.getTime())) {
    return date;
  }

  return parsed.toLocaleDateString("en-IN", {
    day: "numeric",
    month: "long",
    year: "numeric",
  });
}

export default function EmployeeProfile() {
  const { token } = useAuth();
  const navigate = useNavigate();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();

    async function loadProfile() {
      try {
        setLoading(true);
        setError("");

        const response = await getMyProfile(token);
        if (!controller.signal.aborted) {
          setProfile(response);
        }
      } catch (err) {
        if (!controller.signal.aborted) {
          setError(
            err.response?.status === 401
              ? "Your session is no longer valid. Please sign in again."
              : "We couldn't load your profile. Please try again."
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadProfile();

    return () => controller.abort();
  }, [token]);

  const fullName = profile
    ? [profile.firstName, profile.lastName]
        .filter(Boolean)
        .join(" ")
    : "";

  const initials = profile
    ? `${profile.firstName?.[0] || ""}${profile.lastName?.[0] || ""}`
        .toUpperCase()
    : "";

  return (
    <div className="min-h-screen bg-[#f7f8fc] px-5 py-8 text-slate-900 sm:px-8">
      <div className="mx-auto max-w-5xl">

        <button
          type="button"
          onClick={() => navigate("/employee/dashboard")}
          className="mb-8 inline-flex items-center gap-2 text-sm font-medium text-slate-500 transition hover:text-indigo-600"
        >
          <ArrowLeft size={17} />
          Back to dashboard
        </button>

        <div className="mb-8">
          <p className="text-xs font-semibold uppercase tracking-widest text-indigo-600">
            Employee workspace
          </p>

          <h1 className="mt-2 text-3xl font-bold tracking-tight">
            My Profile
          </h1>

          <p className="mt-2 text-sm text-slate-500">
            Your personal and employment information.
          </p>
        </div>

        {loading && (
          <div
            role="status"
            className="rounded-2xl border border-slate-200 bg-white p-8 text-sm text-slate-500"
          >
            Loading your profile...
          </div>
        )}

        {error && (
          <div
            role="alert"
            className="rounded-xl border border-red-200 bg-red-50 p-5 text-sm text-red-700"
          >
            {error}
          </div>
        )}

        {!loading && !error && profile && (
          <div className="space-y-6">

            {/* Identity */}
            <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
              <div className="flex flex-col gap-5 sm:flex-row sm:items-center">

                <div className="flex h-20 w-20 shrink-0 items-center justify-center rounded-2xl bg-indigo-50 text-2xl font-semibold text-indigo-700">
                  {initials || <UserRound size={30} />}
                </div>

                <div className="flex-1">
                  <h2 className="text-2xl font-bold tracking-tight">
                    {fullName || "Employee"}
                  </h2>

                  <p className="mt-1 text-sm text-slate-600">
                    {profile.designation || "Designation not provided"}
                  </p>

                  <div className="mt-3 inline-flex rounded-lg border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs font-semibold text-slate-600">
                    {profile.empCode || "Employee code unavailable"}
                  </div>
                </div>
              </div>
            </section>

            <div className="grid gap-6 lg:grid-cols-2">

              {/* Personal information */}
              <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-7">
                <div className="border-b border-slate-100 pb-4">
                  <h3 className="text-base font-semibold">
                    Personal information
                  </h3>
                  <p className="mt-1 text-xs text-slate-500">
                    Contact details associated with your account.
                  </p>
                </div>

                <div className="divide-y divide-slate-100">
                  <ProfileField
                    label="Full name"
                    value={fullName}
                    icon={UserRound}
                  />
                  <ProfileField
                    label="Email address"
                    value={profile.email}
                    icon={Mail}
                  />
                  <ProfileField
                    label="Phone number"
                    value={profile.phone}
                    icon={Phone}
                  />
                </div>
              </section>

              {/* Employment information */}
              <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-7">
                <div className="border-b border-slate-100 pb-4">
                  <h3 className="text-base font-semibold">
                    Employment details
                  </h3>
                  <p className="mt-1 text-xs text-slate-500">
                    Your role and organizational information.
                  </p>
                </div>

                <div className="divide-y divide-slate-100">
                  <ProfileField
                    label="Employee code"
                    value={profile.empCode}
                    icon={Hash}
                  />
                  <ProfileField
                    label="Designation"
                    value={profile.designation}
                    icon={BriefcaseBusiness}
                  />
                  <ProfileField
                    label="Department"
                    value={profile.departmentName}
                    icon={Building2}
                  />
                  <ProfileField
                    label="Reporting manager"
                    value={profile.managerName}
                    icon={Users}
                  />
                  <ProfileField
                    label="Date of joining"
                    value={formatDate(profile.hireDate)}
                    icon={CalendarDays}
                  />
                </div>
              </section>
            </div>

            <p className="text-xs leading-6 text-slate-400">
              This information is maintained by your organization.
              Contact your administrator if any details are incorrect.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
