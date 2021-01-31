import React from "react";
import firebase from "firebase/app";
import logo from "./logo.svg";
import "./App.css";

function ReactApp({ user }) {
  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.js</code> and save to reload.
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
        <div
          style={{
            marginLeft: 100,
            maxWidth: "100%",
            overflow: "auto",
            fontSize: 14,
            textAlign: "left",
          }}
        >
          <pre>{JSON.stringify(user, null, 2)}</pre>
        </div>

        <button onClick={() => firebase.auth().signOut()}>Sign Out</button>
      </header>
    </div>
  );
}

export default ReactApp;
