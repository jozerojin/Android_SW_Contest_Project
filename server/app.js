const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const bodyParser = require('body-parser');
const bcrypt = require('bcrypt');
const cors = require('cors');

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });
const PORT = 2454;

let items = []; // 항목을 저장할 배열

// 가정한 유저 정보 (실제로는 데이터베이스에서 가져와야 함)
const users = [
  {
    username: '20204585',
    passwordHash: '$2b$10$PH/S00yKdpF3uTxqPZpjm.EAPP51KPpwT7EqsxPi.Cq7e7ust55Wm' // bcrypt 해시된 비밀번호: 'password1'
  },
  {
    username: '20214586',
    passwordHash: '$2b$10$qUhby1DMbfy0KzMVQxxv3uzB.X3.8aFOQiUXTm.Gs25BJ71AaT5.e' // bcrypt 해시된 비밀번호: 'password2'
  },
  {
    username: '20194690',
    passwordHash: '$2b$10$r22SaGTBACJ5RnLcWnfoxO/wEJW.CZO6JHfHpqZzHSEe4tLlLzUg.' // bcrypt 해시된 비밀번호: 'password3'
  }
];

// Body parser 설정
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(cors());

// 로그인 요청 처리
app.post('/login', (req, res) => {
  const { username, password } = req.body;
  console.log(`Received login request for username: ${username}`);

  // 유저 검색
  const user = users.find(user => user.username === username);
  console.log(`User found: ${user ? 'Yes' : 'No'}`);

  // 유저가 존재하는지 확인
  if (!user) {
    console.log('User not found.');
    return res.status(404).json({ error: '사용자를 찾을 수 없습니다.' });
  }

  // 비밀번호 비교
  bcrypt.compare(password, user.passwordHash, (err, result) => {
    if (err) {
      console.error('Error during password comparison:', err);
      return res.status(500).json({ error: '비밀번호 비교 중 오류가 발생했습니다.' });
    }

    if (result) {
      // 비밀번호가 일치하는 경우
      console.log('Password match. Login successful.');
      return res.status(200).json({ message: '로그인 성공' });
    } else {
      // 비밀번호가 일치하지 않는 경우
      console.log('Password mismatch. Login failed.');
      return res.status(401).json({ error: '비밀번호가 올바르지 않습니다.' });
    }
  });
});

// 항목 추가 요청 처리
app.post('/items', (req, res) => {
  const item = req.body;
  items.push(item);
  console.log(`Item added: ${JSON.stringify(item)}`); // 항목 추가 로그

  // 모든 연결된 클라이언트에게 메시지 전송
  wss.clients.forEach(client => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(JSON.stringify({ type: 'ADD', item }));
    }
  });

  res.status(201).json(item);
});

// 항목 조회 요청 처리
app.get('/items', (req, res) => {
  res.json(items);
});

// 항목 삭제 요청 처리
app.delete('/items/:id', (req, res) => {
  const id = req.params.id;
  items = items.filter(item => item.id !== id);
  console.log(`Item deleted with id: ${id}`); // 항목 삭제 로그

  // 모든 연결된 클라이언트에게 메시지 전송
  wss.clients.forEach(client => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(JSON.stringify({ type: 'DELETE', id }));
    }
  });

  res.status(204).send();
});

// WebSocket 연결 처리
wss.on('connection', ws => {
  console.log('New WebSocket connection');
  ws.send(JSON.stringify({ type: 'INIT', items }));

  ws.on('message', message => {
    console.log('Received message:', message);
  });
});

// 서버 시작
server.listen(PORT, () => {
  console.log(`서버가 포트 ${PORT}에서 실행 중...`);
});
