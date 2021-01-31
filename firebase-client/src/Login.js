import React, { useCallback } from "react";
import firebase from "firebase/app";
import useWindowPopup from "./window-popup-hook";

function Login() {
  const onAuthenticated = useCallback(
    ({ value }) => firebase.auth().signInWithCustomToken(value),
    []
  );
  const openPopup = useWindowPopup(480, 700, onAuthenticated);

  return (
    <>
      <button
        onClick={() =>
          firebase
            .auth()
            .signInWithPopup(new firebase.auth.GoogleAuthProvider())
        }
      >
        Sign In with Google
      </button>
      <button onClick={() => openPopup("kakao")}>Sign In with Kakao</button>
      <button onClick={() => openPopup("naver")}>Sign In with Naver</button>
    </>
  );
}

export default Login;
