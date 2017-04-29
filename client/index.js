const sendDataToServer = (event) => {
  const headers = new Headers();
  headers.append("Content-Type", "application/json");
  const coordinates = getPosition(event);
  const body = { x: coordinates.x, y: coordinates.y, color: color };
  const options = {
    method: 'PUT',
    headers: headers,
    mode: 'cors',
    cache: 'default',
    body: JSON.stringify(body),
  };

  fetch('/toto', options);
};

// Initialize color to red
let color = "#FF0000";

const svg = document.getElementsByTagName('svg')[0];
const svgNS = svg.namespaceURI;
const elemRect = svg.getBoundingClientRect();

const createCircle = ({x, y, color}) => {
  const realX = Math.round((x - elemRect.left) / 10) * 10;
  const realY = Math.round((y - elemRect.top) / 10) * 10;
  const newCircle = document.createElementNS(svgNS, 'rect');
  newCircle.setAttribute('width', '10');
  newCircle.setAttribute('height', '10');
  newCircle.setAttribute('x', realX);
  newCircle.setAttribute('y', realY);
  newCircle.setAttribute('fill', color);
  requestAnimationFrame(() => svg.appendChild(newCircle));
  return newCircle;
};

const colorInput = document.getElementById('color-input');
colorInput.addEventListener('change', (event) => {
  color = event.target.value;
});

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

document.addEventListener('click', (event) => {
  if (event.target !== colorInput) {
    sendDataToServer(event);
  }
});

const randomGroupId = Math.random();
const evtSource = new EventSource(`/data/${randomGroupId}`);
evtSource.onmessage = function(e) {
  const [x, y, color] = e.data.split(';');
  createCircle({x, y, color});
};