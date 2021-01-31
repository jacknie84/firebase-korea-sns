import React, { useEffect, useState } from "react";
import firebase from "firebase/app";
import "firebase/auth";
import { BrowserRouter, Route } from "react-router-dom";
import Login from "./Login";
import ReactApp from "./ReactApp";
import Callback from "./Callback";

function App() {
  const [isRequireInitFirebase, setIsRequireInitFirebase] = useState(true);
  const [user, setUser] = useState();

  useEffect(() => {
    if (isRequireInitFirebase) {
      fetch(process.env.REACT_APP_FIREBASE_CONFIG_JSON_URL)
        .then((response) => response.json())
        .then((firebaseConfig) => firebase.initializeApp(firebaseConfig))
        .then(() => setIsRequireInitFirebase(false));
      return null;
    } else {
      return firebase.auth().onAuthStateChanged((user) => setUser(user));
    }
  }, [isRequireInitFirebase]);

  return (
    <BrowserRouter>
      <div>
        <Route exact path="/">
          {user ? <ReactApp user={user} /> : <Login />}
        </Route>
        <Route path="/callback">
          <Callback />
        </Route>
      </div>
    </BrowserRouter>
  );
}

export default App;
