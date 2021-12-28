package com.map.socialnetwork.service;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.Message;
import com.map.socialnetwork.domain.Tuple;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.MissingEntityException;
import com.map.socialnetwork.repository.FriendshipRepository;
import com.map.socialnetwork.repository.MessageRepository;
import com.map.socialnetwork.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record Service(UserRepository usersRepository,
                      FriendshipRepository friendshipsRepository,
                      MessageRepository messageRepository) {

    public void addUser(String firstName, String lastName) {
        usersRepository.store(new User(firstName, lastName));
    }

    public void deleteUser(long id) throws MissingEntityException {
        Optional<User> userToDelete = usersRepository.get(id);

        if (userToDelete.isPresent()) {
            usersRepository.delete(userToDelete.get());
            friendshipsRepository.deleteFriendshipsRelatedToUser(userToDelete.get());
        } else {
            throw new MissingEntityException("User doesn't exist!");
        }
    }

    public void updateUser(long id, String newFirstName, String newSecondName) throws MissingEntityException {
        Optional<User> userToRemove = usersRepository.get(id);

        if (userToRemove.isPresent()) {
            usersRepository.update(id, new User(newFirstName, newSecondName));
        } else {
            throw new MissingEntityException("User doesn't exist!");
        }
    }

    public List<User> getUsers() {
        return usersRepository.getAll();
    }

    public Optional<User> getUser(long id) {
        return usersRepository.get(id);
    }


    public void storeNewMessage(String content, List<Long> to, Long from) {
        messageRepository.store(new Message(
                content,
                to.stream().map(usersRepository::get).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()),
                usersRepository.get(from).orElse(User.deletedUser),
                null
        ));
    }

    public void replyMessage(String content, Long from, Long replyId) throws MissingEntityException {
        Optional<Message> message = messageRepository.get(replyId);

        if (message.isPresent()) {
            messageRepository.store(new Message(
                    content,
                    message.get().getTo().stream().filter(user -> usersRepository.get(user.getId()).isPresent()).collect(Collectors.toList()),
                    usersRepository.get(from).orElse(User.deletedUser),
                    message.get()
            ));
        } else {
            throw new MissingEntityException("Reply message doesn't exist!");
        }
    }

    public void replyAllMessage(String content, Long from, Long replyId) throws MissingEntityException {
        Optional<Message> message = messageRepository.get(replyId);

        if (message.isPresent()) {
            List<User> to = message.get().getTo();
            to.add(message.get().getFrom());

            to = to.stream().filter(user -> usersRepository.get(user.getId()).isPresent()).collect(Collectors.toList());
            messageRepository.store(new Message(
                    content,
                    to,
                    usersRepository.get(from).orElse(User.deletedUser),
                    message.get()
            ));
        } else {
            throw new MissingEntityException("Reply message doesn't exist!");
        }
    }

    public void deleteMessage(Long id) throws MissingEntityException {
        Optional<Message> messageToDelete = messageRepository.get(id);

        if (messageToDelete.isPresent()) {
            messageRepository.delete(messageToDelete.get());
        } else {
            throw new MissingEntityException("Message doesn't exist!");
        }
    }

    public List<Message> getConversation(Long firstUserId, Long secondUserId) throws MissingEntityException {
        User firstUser = usersRepository.get(firstUserId).orElse(User.deletedUser);
        User secondUser = usersRepository.get(secondUserId).orElse(User.deletedUser);

        if (firstUser == User.deletedUser) {
            throw new MissingEntityException("First user is missing!");
        }

        if (secondUser == User.deletedUser) {
            throw new MissingEntityException("Second user is missing!");
        }

        return messageRepository.getConversation(firstUser, secondUser);
    }

    public void sendFriendRequest(Long firstUserId, Long secondUserId) throws MissingEntityException {
        User firstUser = usersRepository.get(firstUserId).orElse(User.deletedUser);
        User secondUser = usersRepository.get(secondUserId).orElse(User.deletedUser);

        if (firstUser == User.deletedUser) {
            throw new MissingEntityException("First user is missing!");
        }

        if (secondUser == User.deletedUser) {
            throw new MissingEntityException("Second user is missing!");
        }

        Optional<Friendship> friendship = friendshipsRepository.getFriendship(new Tuple<>(firstUserId, secondUserId));

        if (friendship.isEmpty()) {
            friendshipsRepository.store(new Friendship(firstUser, secondUser));
        }
    }

    public void respondFriendshipRequest(Long firstUserId, Long secondUserId, Friendship.Status newStatus) throws MissingEntityException {
        User firstUser = usersRepository.get(firstUserId).orElse(User.deletedUser);
        User secondUser = usersRepository.get(secondUserId).orElse(User.deletedUser);

        if (firstUser == User.deletedUser) {
            throw new MissingEntityException("First user is missing!");
        }

        if (secondUser == User.deletedUser) {
            throw new MissingEntityException("Second user is missing!");
        }

        Optional<Friendship> friendship = friendshipsRepository.getFriendship(new Tuple<>(firstUserId, secondUserId));

        if (friendship.isPresent()) {
            Friendship updatedFriendship = friendship.get();
            updatedFriendship.respond(secondUser, newStatus);
            friendshipsRepository.update(updatedFriendship);
        }
    }

    public List<User> getFriends(User user) {
        return friendshipsRepository.getFriends(user);
    }

    public List<User> getRequested(User user) {
        return friendshipsRepository.getRequested(user);
    }
}

