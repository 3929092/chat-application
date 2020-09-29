<template>
  <div class="field is-grouped">
    <p class="control is-expanded has-icons-left">
      <input
        :value="currentUser"
        @input="updateCurrentUser"
        @keyup.enter="join()"
        type="text"
        placeholder="Your name here"
        class="input is-small"
        :disabled="isConnected"
      />
      <span class="icon is-small is-left">
        <i class="fas fa-user"></i>
      </span>
    </p>

    <p class="control">
      <button @click="join()" class="button is-primary is-small">
        {{ isConnected ? "Leave" : "Join" }}
      </button>
    </p>
  </div>

  <div v-if="error" class="notification is-danger is-small">{{ error }}</div>

  <strong v-if="isConnected" class="">Connected users</strong>

  <ul>
    <li v-for="(user, index) in users" :key="index">
      {{ user }}
    </li>
  </ul>
</template>

<script>
export default {
  name: 'ChatUsers',
  computed: {
    users() {
      return this.$store.state.users;
    },
    error() {
      return this.$store.state.error;
    },
    isConnected() {
      return this.$store.state.isConnected;
    },
    currentUser() {
      return this.$store.state.currentUser;
    },
  },
  methods: {
    join() {
      if (this.isConnected) {
        this.$store.dispatch('leave', this.$store.state.currentUser);
      } else if (this.currentUser && this.currentUser.length < 25) {
        this.$store.dispatch('join', this.$store.state.currentUser);
      }
    },
    updateCurrentUser(e) {
      this.$store.commit('updateCurrentUser', e.target.value);
    },
  },
};
</script>

<style>
</style>
