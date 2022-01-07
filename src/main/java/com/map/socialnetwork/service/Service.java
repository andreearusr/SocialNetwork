package com.map.socialnetwork.service;

import com.map.socialnetwork.domain.*;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
import com.map.socialnetwork.exceptions.InvalidRequestException;
import com.map.socialnetwork.exceptions.MissingEntityException;
import com.map.socialnetwork.exceptions.ValidatorException;
import com.map.socialnetwork.repository.eventRepository.EventRepository;
import com.map.socialnetwork.repository.friendshipRepository.FriendshipRepository;
import com.map.socialnetwork.repository.messageRepository.MessageRepository;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.Pageable;
import com.map.socialnetwork.repository.userRepository.UserRepository;
import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Observable;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class Service extends Observable {

    private UserRepository userDBRepository;
    private FriendshipRepository friendshipDBRepository;
    private MessageRepository messageDBRepository;
    private EventRepository eventDBRepository;

    public void addUser(String firstName, String lastName) throws ValidatorException, DuplicateEntityException {
        userDBRepository.store(new User(firstName, lastName));
        notifyObservers(User.class);
    }

    public Optional<User> getUser(String firstName, String lastName) {
        return  userDBRepository.getUser(firstName, lastName);
    }

    public void deleteUser(long id) throws MissingEntityException {
        Optional<User> userToDelete = userDBRepository.get(id);

        if (userToDelete.isPresent()) {
            userDBRepository.delete(userToDelete.get());
            friendshipDBRepository.deleteFriendshipsRelatedToUser(userToDelete.get());
        } else {
            throw new MissingEntityException("User doesn't exist!");
        }

        setChanged();
        notifyObservers(User.class);
    }

    public void updateUser(long id, String newFirstName, String newSecondName) throws MissingEntityException, ValidatorException {
        Optional<User> userToRemove = userDBRepository.get(id);

        if (userToRemove.isPresent()) {
            userDBRepository.update(id, new User(newFirstName, newSecondName));
        } else {
            throw new MissingEntityException("User doesn't exist!");
        }

        setChanged();
        notifyObservers(User.class);
    }

    public List<User> getUsers() {
        return userDBRepository.getAll();
    }

    public Page<User> getUsers(Pageable<User> pageable) {
        return userDBRepository.getAll(pageable);
    }

    public Optional<User> getUser(long id) {
        return userDBRepository.get(id);
    }

    public void sendSingleMessage(String content, List<Long> to, Long from) throws ValidatorException, DuplicateEntityException {
        messageDBRepository.store(new Message(
                content,
                to.stream().map(userDBRepository::get).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()),
                userDBRepository.get(from).orElse(User.deletedUser),
                null
        ));

        setChanged();
        notifyObservers(Message.class);
    }

    public void replyMessage(String content, Long from, Long to, Long replyId) throws MissingEntityException, ValidatorException, DuplicateEntityException {
        Optional<Message> message = messageDBRepository.get(replyId);

        if (message.isPresent()) {
            messageDBRepository.store(new Message(
                    content,
                    List.of(userDBRepository.get(to).orElseThrow(() -> new MissingEntityException("User doesn't exist!"))),
                    userDBRepository.get(from).orElse(User.deletedUser),
                    message.get()
            ));
        } else {
            throw new MissingEntityException("Reply message doesn't exist!");
        }

        setChanged();
        notifyObservers(Message.class);
    }

    public void replyAllMessage(String content, Long from, Long replyId) throws MissingEntityException, ValidatorException, DuplicateEntityException {
        Optional<Message> message = messageDBRepository.get(replyId);

        if (message.isPresent()) {
            List<User> to = message.get().getTo();
            to.add(message.get().getFrom());

            to = to.stream().filter(user -> (!user.getId().equals(from) && userDBRepository.get(user.getId()).isPresent()))
                    .collect(Collectors.toList());
            messageDBRepository.store(new Message(
                    content,
                    to,
                    userDBRepository.get(from).orElseThrow(() -> new MissingEntityException("User doesn't exist")),
                    message.get()
            ));
        } else {
            throw new MissingEntityException("Reply message doesn't exist!");
        }

        setChanged();
        notifyObservers(Message.class);
    }

    public void deleteMessage(Long id) throws MissingEntityException {
        Optional<Message> messageToDelete = messageDBRepository.get(id);

        if (messageToDelete.isPresent()) {
            messageDBRepository.delete(messageToDelete.get());
        } else {
            throw new MissingEntityException("Message doesn't exist!");
        }

        setChanged();
        notifyObservers(Message.class);
    }

    public List<Message> getConversation(Long firstUserId, Long secondUserId) throws MissingEntityException {
        User firstUser = userDBRepository.get(firstUserId).orElseThrow(() -> new MissingEntityException("First user is missing!"));
        User secondUser = userDBRepository.get(secondUserId).orElseThrow(() -> new MissingEntityException("Second user is missing!"));

        return messageDBRepository.getConversation(firstUser, secondUser);
    }

    public Page<Message> getConversation(Pageable<Message> pageable, Long firstUserId, Long secondUserId) throws MissingEntityException {
        User firstUser = userDBRepository.get(firstUserId).orElseThrow(() -> new MissingEntityException("First user is missing!"));
        User secondUser = userDBRepository.get(secondUserId).orElseThrow(() -> new MissingEntityException("Second user is missing!"));

        return messageDBRepository.getConversation(pageable, firstUser, secondUser);
    }

    public List<Message> getReceivedMessages(long userId) {
        return messageDBRepository.getReceivedMessages(userId);
    }

    public void sendFriendRequest(Long firstUserId, Long secondUserId) throws MissingEntityException, DuplicateEntityException, ValidatorException {
        User firstUser = userDBRepository.get(firstUserId).orElseThrow(() -> new MissingEntityException("First user is missing!"));
        User secondUser = userDBRepository.get(secondUserId).orElseThrow(() -> new MissingEntityException("Second user is missing!"));

        friendshipDBRepository.store(new Friendship(firstUser, secondUser));
        setChanged();
        notifyObservers(Friendship.class);
    }

    public void respondFriendshipRequest(Long firstUserId, Long secondUserId, Friendship.Status newStatus) throws MissingEntityException, InvalidRequestException, ValidatorException {
        User firstUser = userDBRepository.get(firstUserId).orElseThrow(() -> new MissingEntityException("First user is missing!"));
        User secondUser = userDBRepository.get(secondUserId).orElseThrow(() -> new MissingEntityException("Second user is missing!"));

        Optional<Friendship> friendship = friendshipDBRepository.get(new Tuple<>(firstUser, secondUser));

        if (friendship.isPresent()) {
            Friendship updatedFriendship = friendship.get();
            updatedFriendship.respond(secondUser, newStatus);
            friendshipDBRepository.update(updatedFriendship);
        }

        setChanged();
        notifyObservers(Friendship.class);
    }

    public List<User> getFriends(User user) {
        return friendshipDBRepository.getFriends(user);
    }

    public Page<User> getFriends(Pageable<User> pageable, User user) {
        return friendshipDBRepository.getFriends(pageable, user);
    }

    public List<Friendship> getReceivedRequests(User user) {
        return friendshipDBRepository.getReceivedRequests(user);
    }

    public Page<Friendship> getReceivedRequests(Pageable<Friendship> pageable, User user) {
        return friendshipDBRepository.getReceivedRequests(pageable, user);
    }

    public void removeFriendship(long firstUserId, long secondUserId) throws ValidatorException, MissingEntityException {
        User firstUser = userDBRepository.get(firstUserId).orElseThrow(() -> new MissingEntityException("First user is missing!"));
        User secondUser = userDBRepository.get(secondUserId).orElseThrow(() -> new MissingEntityException("Second user is missing!"));

        Optional<Friendship> friendship = friendshipDBRepository.get(new Tuple<>(firstUser, secondUser));
        Optional<Friendship> friendshipSwap = friendshipDBRepository.get(new Tuple<>(secondUser, firstUser));

        if (friendship.isPresent()) {
            friendship.get().setStatus(Friendship.Status.REJECTED);
            friendshipDBRepository.update(friendship.get());
        } else if (friendshipSwap.isPresent()) {
            friendshipSwap.get().setStatus(Friendship.Status.REJECTED);
            friendshipDBRepository.update(friendshipSwap.get());
        } else {
            throw new MissingEntityException("Friendship doesn't exist!");
        }

        setChanged();
        notifyObservers(Friendship.class);
    }

    public List<User> getUnrelatedUsers(User user) {
        return friendshipDBRepository.getUnrelatedUsers(user);
    }

    public Page<User> getUnrelatedUsers(Pageable<User> pageable, User user) {
        return friendshipDBRepository.getUnrelatedUsers(pageable, user);
    }

    public void retractRequest(User firstUser, User secondUser) throws MissingEntityException, InvalidRequestException {
        Optional<Friendship> friendship = friendshipDBRepository.get(new Tuple<>(firstUser, secondUser));

        if (friendship.isEmpty()) {
            throw new MissingEntityException("Request doesn't exist!");
        }

        if (friendship.get().getStatus() != Friendship.Status.PENDING) {
            throw new InvalidRequestException("Can not retract this request!");
        }

        friendshipDBRepository.delete(friendship.get());

        setChanged();
        notifyObservers(Friendship.class);
    }

    public List<Friendship> getSentPendingRequests(User user) {
        return friendshipDBRepository.getSentPendingRequests(user);
    }

    public Page<Friendship> getSentPendingRequests(Pageable<Friendship> pageable, User user) {
        return friendshipDBRepository.getSentPendingRequests(pageable, user);
    }

    public List<Friendship> getAllFriendshipRequests(long id) {
        return friendshipDBRepository.getAll(id);
    }

    public Page<Friendship> getAllFriendshipRequests(Pageable<Friendship> pageable, long id) {
        return friendshipDBRepository.getAll(pageable, id);
    }

    public void addEvent(Event event) throws ValidatorException, DuplicateEntityException {
        eventDBRepository.store(event);

        setChanged();
        notifyObservers(Event.class);
    }

    public Page<Event> getAllEvents(Pageable<Event> pageable, long id) {
        return eventDBRepository.getAll(pageable, id);
    }

    public List<Event> getAllEvents(long id) {
        return eventDBRepository.getAll(id);
    }

    public void participateToEvent(Event event, long id) throws DuplicateEntityException {
        eventDBRepository.participateToEvent(event, id);

        setChanged();
        notifyObservers(Event.class);

    }

    public void unsubscribeAtEvent(Event event, long id) throws MissingEntityException, DuplicateEntityException {
        eventDBRepository.unsubscribeAtEvent(event, id);

        setChanged();
        notifyObservers(Event.class);
    }

    public void saveConversation(User firstUser, User secondUser, Timestamp start, Timestamp end) throws IOException {
        List<Message> messages = messageDBRepository.getConversationFromTimeSpan(
                firstUser, secondUser, start, end
        );

        StringBuilder body = new StringBuilder();

        for (Message message : messages) {
            body.append("New message from: ").append(secondUser.getFullName()).append("\nContent: \n\"").append(message.getMessage())
                    .append("\"\n").append("Time: ").append(message.getTimestamp()).append("\n\n");
        }

        saveTextToPdf(body.toString());
    }

    public List<Event> getEventsUserSubscriber(long id){
        return eventDBRepository.getEventsUserSubscriber(id);
    }

    public void saveActivity(User user, Timestamp start, Timestamp end) throws IOException {
        List<Message> messages = messageDBRepository.getActivity(user, start, end);
        List<Friendship> friendships = friendshipDBRepository.getActivity(user, start, end);

        StringBuilder body = new StringBuilder();

        for (Message message : messages) {
            body.append("New message from: ").append(message.getFrom().getFullName()).append("\nContent: \n\"")
                    .append(message.getMessage()).append("\"\n").append("Time: ").append(message.getTimestamp()).append("\n\n");
        }

        for (Friendship friendship : friendships) {
            User friend = (friendship.getId().first().getId().equals(user.getId())) ?
                    friendship.getId().second() : friendship.getId().first();

            body.append("You became friend with ").append(friend.getFullName()).append(" on ")
                    .append(friendship.getTimestamp()).append("\n");
        }

        saveTextToPdf(body.toString());
    }

    private void saveTextToPdf(String text) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.setFont(PDType1Font.TIMES_BOLD, 20);
        contentStream.beginText();
        contentStream.newLineAtOffset(25, 700);
        contentStream.setLeading(14.5f);
        List<String> chunks = List.of(text.split("\n"));

        for (int index = 0, availableRows = 25; index < chunks.size(); ++index, --availableRows) {
            if (availableRows == 0) {
                availableRows = 25;
                contentStream.endText();
                contentStream.close();
                page = new PDPage();
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(PDType1Font.TIMES_BOLD, 20);
                contentStream.beginText();
                contentStream.newLineAtOffset(25, 700);
                contentStream.setLeading(14.5f);
            }

            contentStream.showText(chunks.get(index));
            contentStream.newLine();
            contentStream.newLine();
        }

        contentStream.endText();
        contentStream.close();

        document.save("report-" + LocalDateTime.now() + ".pdf");
        document.close();
    }
}

