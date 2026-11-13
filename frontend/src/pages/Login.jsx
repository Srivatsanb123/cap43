import { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { login, register, setAuth } from "../api";

export default function Login({ onLoginSuccess }) {
  const location = useLocation();
  const navigate = useNavigate();
  const mode = location.pathname === "/register" ? "register" : "login";

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [fullName, setFullName] = useState("");
  const [phone, setPhone] = useState("");
  const [role, setRole] = useState("TENANT");
  const [companyName, setCompanyName] = useState("");
  const [trade, setTrade] = useState("PLUMBING");

  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [successMsg, setSuccessMsg] = useState("");

  useEffect(() => {
    setError("");
    setSuccessMsg("");
  }, [location.pathname]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setSuccessMsg("");
    try {
      const res = await login(email, password);
      setAuth(res.token, {
        userId: res.userId,
        role: res.role,
        fullName: res.fullName,
        tenantId: res.tenantId,
        vendorId: res.vendorId,
      });
      if (onLoginSuccess) onLoginSuccess();
    } catch (err) {
      setError(err.message || "Invalid credentials. Please verify your email and password.");
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setSuccessMsg("");
    try {
      const payload = {
        fullName,
        email,
        password,
        phone: phone || null,
        role,
        companyName: role === "VENDOR" ? companyName : null,
        trade: role === "VENDOR" ? trade : null,
      };
      const res = await register(payload);
      setAuth(res.token, {
        userId: res.userId,
        role: res.role,
        fullName: res.fullName,
        tenantId: res.tenantId,
        vendorId: res.vendorId,
      });
      if (onLoginSuccess) onLoginSuccess();
    } catch (err) {
      setError(err.message || "Registration failed. Please check your inputs.");
    } finally {
      setLoading(false);
    }
  };

  const switchMode = (newMode) => {
    navigate(newMode === "register" ? "/register" : "/login");
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-brand">
          <div className="auth-icon-circle">🏢</div>
          <h2>PropLedger</h2>
          <p className="auth-subtitle">
            Rental Property Maintenance Risk & Deposit Dispute Ledger
          </p>
        </div>

        <div className="auth-tabs">
          <button
            type="button"
            className={`auth-tab-btn ${mode === "login" ? "active" : ""}`}
            onClick={() => switchMode("login")}
          >
            Sign In
          </button>
          <button
            type="button"
            className={`auth-tab-btn ${mode === "register" ? "active" : ""}`}
            onClick={() => switchMode("register")}
          >
            Create Account
          </button>
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {successMsg && <div className="alert alert-success">{successMsg}</div>}

        {mode === "login" ? (
          <form onSubmit={handleLogin} className="auth-form">
            <div className="form-group">
              <label htmlFor="login-email">Email Address</label>
              <input
                id="login-email"
                type="email"
                placeholder="name@company.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
              />
            </div>

            <div className="form-group">
              <div className="form-label-row">
                <label htmlFor="login-password">Password</label>
                <button
                  type="button"
                  className="link-btn"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? "Hide" : "Show"}
                </button>
              </div>
              <input
                id="login-password"
                type={showPassword ? "text" : "password"}
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="current-password"
              />
            </div>

            <button type="submit" className="btn-primary auth-submit-btn" disabled={loading}>
              {loading ? "Signing in..." : "Sign In"}
            </button>

            <div className="auth-footer-prompt">
              <span>Don't have an account?</span>
              <button
                type="button"
                className="link-btn-bold"
                onClick={() => switchMode("register")}
              >
                Register here
              </button>
            </div>
          </form>
        ) : (
          <form onSubmit={handleRegister} className="auth-form">
            <div className="form-group">
              <label htmlFor="reg-fullname">Full Name</label>
              <input
                id="reg-fullname"
                type="text"
                placeholder="e.g. John Doe"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="reg-email">Email Address</label>
              <input
                id="reg-email"
                type="email"
                placeholder="name@company.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
              />
            </div>

            <div className="form-row-2col">
              <div className="form-group">
                <label htmlFor="reg-phone">Phone Number</label>
                <input
                  id="reg-phone"
                  type="tel"
                  placeholder="9876543210"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                />
              </div>

              <div className="form-group">
                <div className="form-label-row">
                  <label htmlFor="reg-password">Password</label>
                  <button
                    type="button"
                    className="link-btn"
                    onClick={() => setShowPassword(!showPassword)}
                  >
                    {showPassword ? "Hide" : "Show"}
                  </button>
                </div>
                <input
                  id="reg-password"
                  type={showPassword ? "text" : "password"}
                  placeholder="Min 6 characters"
                  minLength={6}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  autoComplete="new-password"
                />
              </div>
            </div>

            <div className="form-group">
              <label>Account Role</label>
              <div className="role-selector">
                <label className={`role-chip ${role === "TENANT" ? "selected" : ""}`}>
                  <input
                    type="radio"
                    name="role"
                    value="TENANT"
                    checked={role === "TENANT"}
                    onChange={(e) => setRole(e.target.value)}
                  />
                  <span>🏠 Tenant</span>
                </label>
                <label className={`role-chip ${role === "VENDOR" ? "selected" : ""}`}>
                  <input
                    type="radio"
                    name="role"
                    value="VENDOR"
                    checked={role === "VENDOR"}
                    onChange={(e) => setRole(e.target.value)}
                  />
                  <span>🔧 Service Vendor</span>
                </label>
                <label className={`role-chip ${role === "ADMIN" ? "selected" : ""}`}>
                  <input
                    type="radio"
                    name="role"
                    value="ADMIN"
                    checked={role === "ADMIN"}
                    onChange={(e) => setRole(e.target.value)}
                  />
                  <span>🛡️ Operations Admin</span>
                </label>
              </div>
            </div>

            {role === "VENDOR" && (
              <div className="vendor-extra-fields">
                <div className="form-group">
                  <label htmlFor="reg-company">Company / Business Name</label>
                  <input
                    id="reg-company"
                    type="text"
                    placeholder="e.g. Apex Plumbing Solutions"
                    value={companyName}
                    onChange={(e) => setCompanyName(e.target.value)}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="reg-trade">Primary Trade Specialization</label>
                  <select
                    id="reg-trade"
                    value={trade}
                    onChange={(e) => setTrade(e.target.value)}
                  >
                    <option value="PLUMBING">Plumbing</option>
                    <option value="ELECTRICAL">Electrical</option>
                    <option value="HVAC">HVAC & Air Conditioning</option>
                    <option value="CARPENTRY">Carpentry & Woodwork</option>
                    <option value="PAINTING">Painting & Finishing</option>
                    <option value="GENERAL">General Maintenance</option>
                  </select>
                </div>
              </div>
            )}

            <button type="submit" className="btn-primary auth-submit-btn" disabled={loading}>
              {loading ? "Creating Account..." : "Complete Registration"}
            </button>

            <div className="auth-footer-prompt">
              <span>Already have an account?</span>
              <button
                type="button"
                className="link-btn-bold"
                onClick={() => switchMode("login")}
              >
                Sign In
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
