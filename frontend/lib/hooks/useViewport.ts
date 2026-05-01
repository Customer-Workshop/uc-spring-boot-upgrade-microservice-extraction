import React from "react";

const useViewport = () => {
  const [vw, setVW] = React.useState(0);
  const [vh, setVH] = React.useState(0);

  React.useEffect(() => {
    let timeoutId: ReturnType<typeof setTimeout>;

    const setSizes = () => {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        setVW(window.innerWidth);
        setVH(window.innerHeight);
      }, 150);
    };

    setVW(window.innerWidth);
    setVH(window.innerHeight);
    window.addEventListener("resize", setSizes);
    return () => {
      clearTimeout(timeoutId);
      window.removeEventListener("resize", setSizes);
    };
  }, []);

  return { vw, vh };
};

export default useViewport;
