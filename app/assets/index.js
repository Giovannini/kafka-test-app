const toto = (event) => {
  const options = {
    method: 'PUT',
    headers: new Headers(),
    mode: 'cors',
    cache: 'default',
    body: getPosition(event),
  };

  fetch('/toto', options)
    .then(response => response.blob())
    .then(console.log);
};

function getPosition(e) {
  if (e.pageX || e.pageY) {
    return { x: e.pageX, y: e.pageY };
  }
  else {
    return {
      x: e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft,
      y: e.clientY + document.body.scrollTop + document.documentElement.scrollTop
    };
  }
}

document.addEventListener('click', toto);