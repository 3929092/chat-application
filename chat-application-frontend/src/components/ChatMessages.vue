<template>
  <section class="scrollable" ref="container">
    <chat-message
      v-for="(message, index) in messages"
      :key="index"
      :user="message.user"
      :text="message.text"
      :timestamp="message.timestamp"
      :isHistory="message.isHistory"
    ></chat-message>
  </section>
</template>

<script>
import ChatMessage from './ChatMessage.vue';

export default {
  name: 'ChatMessages',
  components: {
    ChatMessage,
  },
  computed: {
    users() {
      return this.$store.state.users;
    },
    chatHistory() {
      return this.$store.state.chatHistory;
    },
  },
  methods: {
    scrollToEnd() {
      const content = this.$refs.container;
      content.scrollTop = content.scrollHeight;
    },
  },
  updated() {
    this.scrollToEnd();
  },

  mounted() {
    this.scrollToEnd();
  },
  data() {
    return {
      messages: this.$store.state.messages,
    };
  },
};
</script>

<style>
.scrollable {
  overflow-y: auto;
  height: 100%;
}
</style>
