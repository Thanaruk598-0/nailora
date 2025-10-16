// common.js

const api = {
  async get(url) {
    const res = await fetch(url);
    if (!res.ok) throw new Error("GET failed");
    return await res.json();
  },
  async post(url, body) {
    const res = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error("POST failed");
    return await res.json();
  },
  async put(url, body) {
    const res = await fetch(url, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error("PUT failed");
    return await res.json();
  },
  async patch(url, body) {
    const res = await fetch(url, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error("PATCH failed");
    return await res.json();
  },
  async del(url) {
    const res = await fetch(url, { method: "DELETE" });
    if (!res.ok) throw new Error("DELETE failed");
  },
  money(n) {
    return Number(n).toLocaleString("en-US", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
  }
};