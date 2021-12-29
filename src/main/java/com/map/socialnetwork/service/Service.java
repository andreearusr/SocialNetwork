package com.map.socialnetwork.service;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.Message;
import com.map.socialnetwork.domain.Tuple;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
import com.map.socialnetwork.exceptions.MissingEntityException;
import com.map.socialnetwork.repository.FriendshipRepository;
import com.map.socialnetwork.repository.MessageRepository;
import com.map.socialnetwork.repository.UserRepository;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Observable;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class Service extends Observable {

    private UserRepository userRepository;
    private FriendshipRepository friendshipRepository;
    private MessageRepository messageRepository;

    public void addUser(String firstName, String lastName) {
        userRepository.store(new User(firstName, lastName));
        notifyObservers();
    }

    public void deleteUser(long id) throws MissingEntityException {
        Optional<User> userToDelete = userRepository.get(id);

        if (userToDelete.isPresent()) {
            userRepository.delete(userToDelete.get());
            friendshipRepository.deleteFriendshipsRelatedToUser(userToDelete.get());
        } else {
            throw new MissingEntityException("User doesn't exist!");
        }

        setChanged();
        notifyObservers();
    }

    public void updateUser(long id, String newFirstName, String newSecondName) throws MissingEntityException {
        Optional<User> userToRemove = userRepository.get(id);

        if (userToRemove.isPresent()) {
            userRepository.update(id, new User(newFirstName, newSecondName));
        } else {
            throw new MissingEntityException("User doesn't exist!");
        }

        setChanged();
        notifyObservers();
    }

    public List<User> getUsers() {
        return userRepository.getAll();
    }

    public Optional<User> getUser(long id) {
        return userRepository.get(id);
    }


    public void storeNewMessage(String content, List<Long> to, Long from) {
        messageRepository.store(new Message(
                content,
                to.stream().map(userRepository::get).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()),
                userRepository.get(from).orElse(User.deletedUser),
                null
        ));

        setChanged();
        notifyObservers();
    }

    public void replyMessage(String content, Long from, Long replyId) throws MissingEntityException {
        Optional<Message> message = messageRepository.get(replyId);

        if (message.isPresent()) {
            messageRepository.store(new Message(
                    content,
                    message.get().getTo().stream().filter(user -> userRepository.get(user.getId()).isPresent()).collect(Collectors.toList()),
                    userRepository.get(from).orElse(User.deletedUser),
                    message.get()
            ));
        } else {
            throw new MissingEntityException("Reply message doesn't exist!");
        }

        setChanged();
        notifyObservers();
    }

    public void replyAllMessage(String content, Long from, Long replyId) throws MissingEntityException {
        Optional<Message> message = messageRepository.get(replyId);

        if (message.isPresent()) {
            List<User> to = message.get().getTo();
            to.add(message.get().getFrom());

            to = to.stream().filter(user -> userRepository.get(user.getId()).isPresent()).collect(Collectors.toList());
            messageRepository.store(new Message(
                    content,
                    to,
                    userRepository.get(from).orElse(User.deletedUser),
                    message.get()
            ));
        } else {
            throw new MissingEntityException("Reply message doesn't exist!");
        }

        setChanged();
        notifyObservers();
    }

    public void deleteMessage(Long id) throws MissingEntityException {
        Optional<Message> messageToDelete = messageRepository.get(id);

        if (messageToDelete.isPresent()) {
            messageRepository.delete(messageToDelete.get());
        } else {
            throw new MissingEntityException("Message doesn't exist!");
        }

        setChanged();
        notifyObservers();
    }

    public List<Message> getConversation(Long firstUserId, Long secondUserId) throws MissingEntityException {
        User firstUser = userRepository.get(firstUserId).orElse(User.deletedUser);
        User secondUser = userRepository.get(secondUserId).orElse(User.deletedUser);

        if (firstUser == User.deletedUser) {
            throw new MissingEntityException("First user is missing!");
        }

        if (secondUser == User.deletedUser) {
            throw new MissingEntityException("Second user is missing!");
        }

        return messageRepository.getConversation(firstUser, secondUser);
    }

    public void sendFriendRequest(Long firstUserId, Long secondUserId) throws MissingEntityException, DuplicateEntityException {
        User firstUser = userRepository.get(firstUserId).orElse(User.deletedUser);
        User secondUser = userRepository.get(secondUserId).orElse(User.deletedUser);

        if (firstUser == User.deletedUser) {
            throw new MissingEntityException("First user is missing!");
        }

        if (secondUser == User.deletedUser) {
            throw new MissingEntityException("Second user is missing!");
        }

        friendshipRepository.store(new Friendship(firstUser, secondUser));
        setChanged();
        notifyObservers();
    }

    public void respondFriendshipRequest(Long firstUserId, Long secondUserId, Friendship.Status newStatus) throws MissingEntityException {
        User firstUser = userRepository.get(firstUserId).orElse(User.deletedUser);
        User secondUser = userRepository.get(secondUserId).orElse(User.deletedUser);

        if (firstUser == User.deletedUser) {
            throw new MissingEntityException("First user is missing!");
        }

        if (secondUser == User.deletedUser) {
            throw new MissingEntityException("Second user is missing!");
        }

        Optional<Friendship> friendship = friendshipRepository.getFriendship(new Tuple<>(firstUserId, secondUserId));

        if (friendship.isPresent()) {
            Friendship updatedFriendship = friendship.get();
            updatedFriendship.respond(secondUser, newStatus);
            friendshipRepository.update(updatedFriendship);
        }

        setChanged();
        notifyObservers();
    }

    public List<User> getFriends(User user) {
        return friendshipRepository.getFriends(user);
    }

    public List<User> getRequested(User user) {
        return friendshipRepository.getRequested(user);
    }

    public void removeFriendship(long firstUserId, long secondUserId) throws MissingEntityException {
        if (firstUserId > secondUserId) {
            long aux = firstUserId;
            firstUserId = secondUserId;
            secondUserId = aux;
        }
        Optional<Friendship> friendship = friendshipRepository.getFriendship(new Tuple<>(firstUserId, secondUserId));
        friendship.get().setStatus(Friendship.Status.REJECTED);
        friendshipRepository.update(friendship.get());
        setChanged();
        notifyObservers();
    }

    public List<User> getUnrelatedUsers(User user) {
        return friendshipRepository.getUnrelatedUsers(user);
    }

    public void setFriendshipStatus(long firstUserId, long secondUserId, Friendship.Status newStatus) throws MissingEntityException {
        if (firstUserId > secondUserId) {
            long aux = firstUserId;
            firstUserId = secondUserId;
            secondUserId = aux;
        }

        Optional<Friendship> friendship = friendshipRepository.getFriendship(new Tuple<>(firstUserId, secondUserId));
        friendship.get().setStatus(newStatus);
        friendshipRepository.update(friendship.get());

        setChanged();
        notifyObservers();
    }
}

