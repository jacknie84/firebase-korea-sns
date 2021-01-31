import React, { useEffect, useMemo } from "react";
import { useLocation } from "react-router-dom";

const baseUrl = process.env.REACT_APP_SERVER_ENDPOINT_BASE_URL;
const clientId = process.env.REACT_APP_SERVER_ENDPOINT_CLIENT_ID;
const clientSecret = process.env.REACT_APP_SERVER_ENDPOINT_CLIENT_SECRET;

function Callback() {
  const { search } = useLocation();
  const query = useMemo(() => new URLSearchParams(search), [search]);

  useEffect(() => {
    const code = query.get("code");
    const clientRegistrationId = query.get("clientRegistrationId");
    const e = encodeURIComponent;
    fetch(`${baseUrl}/firebase/${e(clientRegistrationId)}/custom-token`, {
      method: "post",
      mode: "cors",
      credentials: "include",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
        Authorization: `Basic ${btoa(`${clientId}:${clientSecret}`)}`,
      },
      body: `code=${e(code)}`,
    })
      .then((response) => response.json())
      .then((firebaseCustomToken) =>
        window.opener.postMessage(
          { type: "firebaseCustomToken", payload: firebaseCustomToken },
          "*"
        )
      )
      .then(() => window.close());
  }, [query]);

  return <h1>Wating...</h1>;
}

export default Callback;
