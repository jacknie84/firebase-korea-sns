import { useCallback, useEffect } from "react";

function useWindowPopup(width, height, onAuthenticated) {
  const openPopup = useCallback(
    (clientRegistrationId) => {
      const url = `http://localhost:8080/oauth2/authorization/${clientRegistrationId}`;
      const target = `${clientRegistrationId}-auth`;
      const top = window.screen.height / 2 - height / 2;
      const left = window.screen.width / 2 - width / 2;
      return window.open(
        url,
        target,
        `status=no, height=${height}, width=${width}, left=${left}, top=${top}`
      );
    },
    [width, height]
  );

  useEffect(() => {
    const eventListener = (event) => {
      if (event.data.type === "firebaseCustomToken") {
        onAuthenticated(event.data.payload);
      }
    };
    window.addEventListener("message", eventListener);
    return () => window.removeEventListener("message", eventListener);
  }, [onAuthenticated]);

  return openPopup;
}

export default useWindowPopup;
