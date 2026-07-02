const { app, BrowserWindow } = require('electron');
const path = require('path');
const { spawn } = require('child_process');

let mainWindow;
let backendProcess;

function startBackend() {
  // 获取 JAR 路径（开发时使用当前目录，打包后使用 resources 目录）
  const jarPath = process.resourcesPath
    ? path.join(process.resourcesPath, 'backend.jar')
    : path.join(__dirname, 'backend.jar');

  backendProcess = spawn('java', ['-jar', jarPath], {
    cwd: path.dirname(jarPath),
    stdio: 'pipe'
  });

  backendProcess.stdout.on('data', (data) => {
    console.log(`[Backend] ${data}`);
  });

  backendProcess.stderr.on('data', (data) => {
    console.error(`[Backend Error] ${data}`);
  });

  backendProcess.on('close', (code) => {
    console.log(`Backend exited with code ${code}`);
  });
}

function waitForBackend(url, callback) {
  const http = require('http');
  const check = () => {
    http.get(url, (res) => {
      if (res.statusCode === 200) callback();
      else setTimeout(check, 1000);
    }).on('error', () => setTimeout(check, 1000));
  };
  check();
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1366,
    height: 768,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true
    }
  });

  // 等待后端就绪后加载页面
  waitForBackend('http://localhost:8080', () => {
    mainWindow.loadURL('http://localhost:8080');
  });
}

app.whenReady().then(() => {
  startBackend();
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});

app.on('before-quit', () => {
  if (backendProcess) {
    backendProcess.kill();
  }
});