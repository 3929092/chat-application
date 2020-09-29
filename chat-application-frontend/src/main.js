import { createApp } from 'vue';
import { createStore } from 'vuex';
import App from './App.vue';

const webSocket = new WebSocket(`ws://${process.env.VUE_APP_CHAT_HOST}/chat`);

const app = createApp(App);

const store = createStore({
  state() {
    return {
      users: [],
      currentUser: '',
      messages: [],
      currentMessage: '',
      error: '',
      chatHistory: '',
      isConnected: false,
    };
  },
  actions: {
    join({ dispatch }, name) {
      dispatch('sendMessage', { type: 'join', user: name });
    },
    say({ commit, dispatch, state }, text) {
      dispatch('sendMessage', { type: 'say', user: state.currentUser, text });
      commit('updateCurrentMessage', '');
    },
    leave({ commit, dispatch, state }) {
      dispatch('sendMessage', { type: 'leave', user: state.currentUser });
      commit('resetUsers');
      commit('disconnect');
    },
    sendMessage({ state }, payload) {
      webSocket.send(JSON.stringify(payload));
    },
    receiveMessage({ commit }, payload) {
      commit('connect');

      if (payload.type === 'say') {
        commit('addMessage', { ...payload.data, isHistory: false });
      } else if (payload.type === 'join') {
        commit('addUser', payload.data);
      } else if (payload.type === 'users') {
        commit('showError', '');
        commit('resetUsers');
        payload.data.forEach((user) => commit('addUser', user));
      } else if (payload.type === 'left') {
        commit('removeUser', payload.data);
      } else if (payload.type === 'error') {
        commit('showError', payload.data);
      } else if (payload.type === 'history') {
        payload.data.forEach((item) => {
          commit('addMessage', { ...item, isHistory: true });
        });
      }
    },
  },
  mutations: {
    addMessage(state, data) {
      state.messages.push({
        user: data.user, text: data.text, timestamp: data.timestamp, isHistory: data.isHistory,
      });
    },
    updateCurrentMessage(state, text) {
      state.currentMessage = text;
    },
    addUser(state, user) {
      state.users.push(user);
    },
    removeUser(state, user) {
      state.users.splice(state.users.indexOf(user), 1);
    },
    resetUsers(state) {
      state.users = [];
    },
    showError(state, error) {
      state.error = error;
    },
    updateCurrentUser(state, username) {
      state.currentUser = username;
    },
    updateChatHistory(state, chatHistory) {
      state.chatHistory = chatHistory;
    },
    connect(state) {
      state.isConnected = true;
    },
    disconnect(state) {
      state.isConnected = false;
    },
  },
});

webSocket.onmessage = (message) => {
  store.dispatch('receiveMessage', JSON.parse(message.data));
};

window.addEventListener('unload', () => {
  store.dispatch('leave');
});

app.use(store);
app.mount('#app');
