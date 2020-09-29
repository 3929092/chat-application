<template>
  <chat-header />
  <div class="columns main">
    <div class="column is-two-thirds">
      <chat-messages />
    </div>
    <div class="column">
      <chat-users />
    </div>
  </div>
  <chat-input />
</template>

<script>
import ChatHeader from './ChatHeader.vue';
import ChatMessages from './ChatMessages.vue';
import ChatUsers from './ChatUsers.vue';
import ChatInput from './ChatInput.vue';

export default {
  name: 'ChatApplication',
  components: {
    ChatHeader,
    ChatMessages,
    ChatUsers,
    ChatInput,
  },
};
</script>

<style>
.main {
  height: 60%;
  margin-top: 1.5em;
}
</style>
