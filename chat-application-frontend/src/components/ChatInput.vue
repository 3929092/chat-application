<template>
  <div class="field is-grouped">
    <p class="control is-expanded has-icons-left">
      <input
        v-model="currentMessage"
        @keyup.enter="send()"
        type="text"
        placeholder="Chat here"
        class="input"
      />
      <span class="icon is-small is-left">
        <i class="fas fa-comment-dots"></i>
      </span>
    </p>
    <p class="control">
      <button
        @click="send()"
        class="button is-primary"
        :disabled="
          !isConnected || !currentMessage || currentMessage.length > 200
        "
      >
        Send
      </button>
    </p>
  </div>
</template>

<script>
export default {
  name: 'ChatInput',
  computed: {
    currentMessage: {
      get() {
        return this.$store.state.currentMessage;
      },
      set(value) {
        this.$store.commit('updateCurrentMessage', value);
      },
    },
    isConnected() {
      return this.$store.state.isConnected;
    },
  },
  methods: {
    send() {
      if (this.currentMessage) {
        this.$store.dispatch('say', this.currentMessage);
      }
    },
  },
};
</script>

<style>
</style>
