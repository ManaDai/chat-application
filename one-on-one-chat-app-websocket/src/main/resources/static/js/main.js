'use strict';

const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const connectingElement = document.querySelector('.connecting');
const chatArea = document.querySelector('#chat-messages');
const logout = document.querySelector('#logout');

let stompClient = null;
let nickname = null;
let fullname = null;
let selectedUserId = null;
let selectedMessage = null; 

// 接続するためのメソッド
function connect(event) {
    nickname = document.querySelector('#nickname').value.trim();
    fullname = document.querySelector('#fullname').value.trim();

    if (nickname && fullname) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}

// WebSocketの接続が確立した後の処理を定義
function onConnected() {
    // 自分宛てのメッセージキュー
    stompClient.subscribe(`/user/${nickname}/queue/messages`, onMessageReceived);
    // 公開メッセージ用のサブスクリプション
    stompClient.subscribe(`/user/public`, onMessageReceived);
    //メッセージを削除した際のサブスクリプション
    stompClient.subscribe(`/topic/messageDeleted`, onMessageDeleted);

    // register the connected user
    stompClient.send("/app/user.addUser",
        {},
        JSON.stringify({ nickName: nickname, fullName: fullname, status: 'ONLINE' })
    );
    document.querySelector('#connected-user-fullname').textContent = fullname;
    findAndDisplayConnectedUsers().then();
}

// コネクト済みのユーザーの表示のためのメソッド
async function findAndDisplayConnectedUsers() {
    const connectedUsersResponse = await fetch('/users');
    let connectedUsers = await connectedUsersResponse.json();
    connectedUsers = connectedUsers.filter(user => user.nickName !== nickname);
    const connectedUsersList = document.getElementById('connectedUsers');
    connectedUsersList.innerHTML = '';

    connectedUsers.forEach(user => {
        appendUserElement(user, connectedUsersList);
        if (connectedUsers.indexOf(user) < connectedUsers.length - 1) {
            const separator = document.createElement('li');
            separator.classList.add('separator');
            connectedUsersList.appendChild(separator);
        }
    });
}

//コネクト済みのユーザーをappendするためのメソッド
function appendUserElement(user, connectedUsersList) {
    const listItem = document.createElement('li');
    listItem.classList.add('user-item');
    listItem.id = user.nickName;

    const userImage = document.createElement('img');
    userImage.src = '../img/user_icon2.png';
    userImage.alt = user.fullName;

    const usernameSpan = document.createElement('span');
    usernameSpan.textContent = user.fullName;

    const receivedMsgs = document.createElement('span');
    receivedMsgs.textContent = '0';
    receivedMsgs.classList.add('nbr-msg', 'hidden');

    listItem.appendChild(userImage);
    listItem.appendChild(usernameSpan);
    listItem.appendChild(receivedMsgs);

    listItem.addEventListener('click', userItemClick);

    connectedUsersList.appendChild(listItem);
}

// 接続中のユーザーアイテムがクリックされた際の処理を定義
function userItemClick(event) {
    document.querySelectorAll('.user-item').forEach(item => {
        item.classList.remove('active');
    });
    messageForm.classList.remove('hidden');

    const clickedUser = event.currentTarget;
    clickedUser.classList.add('active');

    selectedUserId = clickedUser.getAttribute('id');
    fetchAndDisplayUserChat().then();

    const nbrMsg = clickedUser.querySelector('.nbr-msg');
    nbrMsg.classList.add('hidden');
    nbrMsg.textContent = '0';

}


async function fetchAndDisplayUserChat() {
    const userChatResponse = await fetch(`/messages/${nickname}/${selectedUserId}`);
    const userChat = await userChatResponse.json();
    chatArea.innerHTML = '';
    userChat.forEach(chat => {
        displayMessage(chat.senderId, chat.content, chat.timestamp, chat.id);
    });
    chatArea.scrollTop = chatArea.scrollHeight;
}


//messageIDはchatひとつのidのこと
function displayMessage(senderId, content, timestamp, messageId) {
    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message');


    //長押しによるオプション作成
    messageContainer.oncontextmenu = (event) => showContextMenu(event, messageContainer);
    messageContainer.ontouchstart = (event) => startLongPress(event, messageContainer);
    messageContainer.ontouchend = cancelLongPress;


    // メッセージ送信者に応じてクラスを設定
    if (senderId === nickname) {
        messageContainer.classList.add('sender');
    } else {
        messageContainer.classList.add('receiver');
    }

    const messageText = document.createElement('p');
    messageText.textContent = content;

    // メッセージの内容を追加
    messageContainer.appendChild(messageText);

    // メッセージの時間を表示する要素を作成し追加
    const messageTime = document.createElement('span');
    messageTime.classList.add('message-time');
    messageTime.textContent = formatMessageTime(timestamp);
    messageContainer.appendChild(messageTime);

    //hiddenでmessageIdを入れる
    const hiddenMessageIdInput = document.createElement('input');
    hiddenMessageIdInput.setAttribute('name', 'message-id');
    hiddenMessageIdInput.classList.add('hidden');
    hiddenMessageIdInput.value = messageId;
    messageContainer.appendChild(hiddenMessageIdInput);


    // チャットエリアに追加
    const chatArea = document.getElementById('chat-messages');
    chatArea.appendChild(messageContainer);
}

// メッセージ送信時刻をフォーマットする関数
function formatMessageTime(timestamp) {
    // タイムスタンプをミリ秒からDateオブジェクトに変換
    const date = new Date(timestamp);

    // 時間と分を取得
    const hours = String(date.getHours()).padStart(2, '0'); // 2桁の0パディング
    const minutes = String(date.getMinutes()).padStart(2, '0'); // 2桁の0パディング

    // フォーマットされた時間を返す
    return `${hours}:${minutes}`;

}

// コンテキストメニューを表示する関数
function showContextMenu(event, messageContainer) {
    event.preventDefault();
    selectedMessage = messageContainer;// 選択されたメッセージ要素を保存

    const contextMenu = document.getElementById('context-menu');

    contextMenu.style.top = `${event.clientY}px`;  // コンテキストメニューの表示位置（Y座標）を設定
    contextMenu.style.left = `${event.clientX}px`;  // コンテキストメニューの表示位置（X座標）を設定
    contextMenu.style.display = 'block';  // コンテキストメニューを表示
}

// 長押しイベントの開始
function startLongPress(event, messageContainer) {
    selectedMessage = messageContainer;
    this.longPressTimeout = setTimeout(() => {
        const touch = event.touches[0];
        const contextMenu = document.getElementById('context-menu');
        contextMenu.style.top = `${touch.clientY}px`;
        contextMenu.style.left = `${touch.clientX}px`;
        contextMenu.style.display = 'block';
    }, 800);
}

// 長押しイベントのキャンセル
function cancelLongPress() {
    clearTimeout(this.longPressTimeout);
}



// メッセージ削除関数 
async function deleteMessage() {
    if (selectedMessage) {
        const messageId = selectedMessage.querySelector('input[name="message-id"]').value;

        try {
            const response = await fetch(`/messages/${messageId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Failed to delete message');
            }

            selectedMessage.remove();
            selectedMessage = null;
            document.getElementById('context-menu').style.display = 'none';
        } catch (error) {
            console.error('Error deleting message:', error);
        }
    }
}


// コンテキストメニューを閉じる
document.addEventListener('click', () => {
    document.getElementById('context-menu').style.display = 'none';
});



function onError() {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}


function sendMessage(event) {
    const messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        const randomId = Math.random().toString(36).substr(2, 9);
        const chatMessage = {
            senderId: nickname,
            recipientId: selectedUserId,
            content: messageContent,
            timestamp: new Date(),
            id: randomId // ランダムなIDを設定
        };
        stompClient.send("/app/chat", {}, JSON.stringify(chatMessage));
        displayMessage(nickname, messageContent, chatMessage.timestamp, randomId);
        messageInput.value = '';
    }
    chatArea.scrollTop = chatArea.scrollHeight;
    event.preventDefault();
}


async function onMessageReceived(payload) {
    await findAndDisplayConnectedUsers();
    console.log('Message received', payload);
    const message = JSON.parse(payload.body);
    if (selectedUserId && selectedUserId === message.senderId) {
        displayMessage(message.senderId, message.content, message.timestamp, message.id);
        chatArea.scrollTop = chatArea.scrollHeight;
    }

    if (selectedUserId) {
        document.querySelector(`#${selectedUserId}`).classList.add('active');
    } else {
        messageForm.classList.add('hidden');
    }

    const notifiedUser = document.querySelector(`#${message.senderId}`);
    if (notifiedUser && !notifiedUser.classList.contains('active')) {
        const nbrMsg = notifiedUser.querySelector('.nbr-msg');
        nbrMsg.classList.remove('hidden');
        nbrMsg.textContent = '';
    }
}


// メッセージ削除の通知を処理する関数
function onMessageDeleted(payload) {
    console.log('Delete message received:', payload);
    let deletedMessageId = payload.body;
    try {
        deletedMessageId = JSON.parse(deletedMessageId);  // 二重シリアライズの場合は一度パース
    } catch (e) {
        console.error('Failed to parse deleted message ID:', e);
    }
    // 画面上の削除対象メッセージを特定し、非表示にする処理を実行
    const messageContainers = document.querySelectorAll('.message');
    messageContainers.forEach(container => {
        const messageIdInput = container.querySelector('input[name="message-id"]');
        if (messageIdInput && messageIdInput.value === deletedMessageId) {
            container.remove(); // メッセージを削除
        }
    });
}

function onLogout() {
    stompClient.send("/app/user.disconnectUser",
        {},
        JSON.stringify({ nickName: nickname, fullName: fullname, status: 'OFFLINE' })
    );
    window.location.reload();
}



function formatMessageTime(timestamp) {
    // タイムスタンプをミリ秒からDateオブジェクトに変換
    const date = new Date(timestamp);

    // 日時を適切なフォーマットに変換（例: "HH:MM"）
    const formattedTime = `${date.getHours()}:${date.getMinutes()}`;

    return formattedTime;
}


function changeTheme(theme) {
    const themeLink = document.getElementById('theme-link');
    themeLink.href = `https://stackpath.bootstrapcdn.com/bootswatch/4.5.2/${theme}/bootstrap.min.css`;
}

// user-formがhiddenになった後にtheme-selectorを表示する
document.addEventListener('DOMContentLoaded', function () {
    const userForm = document.getElementById('username-page');
    const themeSelector = document.getElementById('theme-selector');

    // フォームがhiddenクラスを持っているかを監視する方法
    const observer = new MutationObserver(function (mutations) {
        mutations.forEach(function (mutation) {
            if (mutation.attributeName === 'class' && mutation.target.classList.contains('hidden')) {
                themeSelector.style.display = 'block';
                observer.disconnect(); // 必要なら監視を停止する
            }
        });
    });

    // 監視を開始する
    observer.observe(userForm, { attributes: true });
});


// +マークをクリックしたらスタンプメニューを表示する
const stampToggleButton = document.getElementById('stamp-toggle-button');
const stampMenu = document.getElementById('stamp-menu');

stampToggleButton.addEventListener('click', function (event) {
    if (stampMenu.classList.contains('hidden')) {
        stampMenu.classList.remove('hidden');
    } else {
        stampMenu.classList.add('hidden');
    }

    // スタンプメニューを表示したら、他のメニュー（例：コンテキストメニュー）を閉じる
    const contextMenu = document.getElementById('context-menu');
    if (!stampMenu.classList.contains('hidden')) {
        contextMenu.style.display = 'none';
    }
    event.stopPropagation(); // イベントの伝播を止める
});



// スタンプを選択して挿入する
function insertStamp(stamp) {
    const messageInput = document.getElementById('message');
    messageInput.value += stamp;
}




usernameForm.addEventListener('submit', connect, true); // step 1
messageForm.addEventListener('submit', sendMessage, true);
logout.addEventListener('click', onLogout, true);
window.onbeforeunload = () => onLogout();