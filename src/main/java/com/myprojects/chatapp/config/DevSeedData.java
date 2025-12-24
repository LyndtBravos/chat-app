package com.myprojects.chatapp.config;

import com.myprojects.chatapp.entity.*;
import com.myprojects.chatapp.repository.ChatParticipantRepository;
import com.myprojects.chatapp.repository.ChatRepository;
import com.myprojects.chatapp.repository.MessageRepository;
import com.myprojects.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevSeedData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        if (userRepository.count() > 0)
            return;

        User alice = createUser("alice", "alice@test.com", "0623458264");
        User bob   = createUser("bob", "bob@test.com", "0623458274");
        User carl  = createUser("carl", "carl@test.com", "0623458284");
        User dana  = createUser("dana", "dana@test.com","0623458294");

        userRepository.saveAll(List.of(alice, bob, carl, dana));

        Chat privateChat = Chat.builder()
                .name("Alice & Bob")
                .isGroup(false)
                .type(ChatType.PRIVATE)
                .createdAt(LocalDateTime.now())
                .build();

        Chat groupChat = Chat.builder()
                .name("Dev Group")
                .isGroup(true)
                .type(ChatType.GROUP)
                .createdAt(LocalDateTime.now())
                .build();

        chatRepository.saveAll(List.of(privateChat, groupChat));

        addParticipant(privateChat, alice);
        addParticipant(privateChat, bob);

        addParticipant(groupChat, alice);
        addParticipant(groupChat, bob);
        addParticipant(groupChat, carl);
        addParticipant(groupChat, dana);

        messageRepository.saveAll(List.of(
                message(privateChat, alice, "Hey Bob 👋", MessageStatus.READ),
                message(privateChat, bob, "Hey Alice!", MessageStatus.READ),
                message(privateChat, alice, "You online?", MessageStatus.DELIVERED),
                message(privateChat, bob, "Yep, what's up?", MessageStatus.SENT),

                message(groupChat, alice, "Welcome to the dev group 🚀", MessageStatus.READ),
                message(groupChat, carl, "Nice!", MessageStatus.DELIVERED),
                message(groupChat, dana, "Hello everyone", MessageStatus.SENT),
                message(groupChat, bob, "Oops, failed send", MessageStatus.FAILED)
        ));
    }

    private User createUser(String username, String email, String phoneNumber) {
        return User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("password"))
                .lastSeen(LocalDateTime.now())
                .phoneNumber(phoneNumber)
                .build();
    }

    private void addParticipant(Chat chat, User user) {
        participantRepository.save(
                ChatParticipant.builder()
                        .chat(chat)
                        .user(user)
                        .build()
        );
    }

    private Message message(
            Chat chat,
            User sender,
            String content,
            MessageStatus status
    ) {
        return Message.builder()
                .chat(chat)
                .sender(sender)
                .content(content)
                .status(status)
                .createdAt(LocalDateTime.now().minusMinutes((long) (Math.random() * 60)))
                .build();
    }
}