package com.campaignpro.service;

import com.campaignpro.dto.ContactDTO;
import com.campaignpro.dto.ContactGroupDTO;
import com.campaignpro.exception.BadRequestException;
import com.campaignpro.exception.ResourceNotFoundException;
import com.campaignpro.model.Contact;
import com.campaignpro.model.ContactGroup;
import com.campaignpro.repository.ContactGroupRepository;
import com.campaignpro.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ContactGroupRepository contactGroupRepository;

    public List<ContactDTO> getAllContacts(String userId) {
        return contactRepository.findByUserId(userId).stream()
                .map(this::convertToContactDTO)
                .collect(Collectors.toList());
    }

    public ContactDTO getContactById(String contactId, String userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", "id", contactId));

        if (!contact.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to access this contact");
        }

        return convertToContactDTO(contact);
    }

    public ContactDTO createContact(String userId, ContactDTO contactDTO) {
        Contact contact = new Contact(userId, contactDTO.getFirstName(), contactDTO.getLastName(), contactDTO.getEmail());
        contact.setPhone(contactDTO.getPhone());
        contact.setGroups(contactDTO.getGroups());
        Contact savedContact = contactRepository.save(contact);
        return convertToContactDTO(savedContact);
    }

    public ContactDTO updateContact(String contactId, String userId, ContactDTO contactDTO) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", "id", contactId));

        if (!contact.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to update this contact");
        }

        if (contactDTO.getFirstName() != null) {
            contact.setFirstName(contactDTO.getFirstName());
        }
        if (contactDTO.getLastName() != null) {
            contact.setLastName(contactDTO.getLastName());
        }
        if (contactDTO.getEmail() != null) {
            contact.setEmail(contactDTO.getEmail());
        }
        if (contactDTO.getPhone() != null) {
            contact.setPhone(contactDTO.getPhone());
        }
        if (contactDTO.getGroups() != null) {
            contact.setGroups(contactDTO.getGroups());
        }

        Contact updatedContact = contactRepository.save(contact);
        return convertToContactDTO(updatedContact);
    }

    public void deleteContact(String contactId, String userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", "id", contactId));

        if (!contact.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to delete this contact");
        }

        contactRepository.delete(contact);
    }

    public List<ContactDTO> searchContacts(String userId, String searchTerm) {
        return contactRepository.searchContacts(userId, searchTerm).stream()
                .map(this::convertToContactDTO)
                .collect(Collectors.toList());
    }

    public List<ContactDTO> getContactsByGroup(String userId, String groupName) {
        return contactRepository.findByUserIdAndGroupsContaining(userId, groupName).stream()
                .map(this::convertToContactDTO)
                .collect(Collectors.toList());
    }

    // Contact Group Methods
    public List<ContactGroupDTO> getAllGroups(String userId) {
        return contactGroupRepository.findByUserId(userId).stream()
                .map(this::convertToContactGroupDTO)
                .collect(Collectors.toList());
    }

    public ContactGroupDTO createGroup(String userId, ContactGroupDTO groupDTO) {
        if (contactGroupRepository.existsByUserIdAndName(userId, groupDTO.getName())) {
            throw new BadRequestException("Group with this name already exists");
        }

        ContactGroup group = new ContactGroup(userId, groupDTO.getName(), groupDTO.getDescription());
        ContactGroup savedGroup = contactGroupRepository.save(group);
        return convertToContactGroupDTO(savedGroup);
    }

    public ContactGroupDTO updateGroup(String groupId, String userId, ContactGroupDTO groupDTO) {
        ContactGroup group = contactGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ContactGroup", "id", groupId));

        if (!group.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to update this group");
        }

        if (groupDTO.getName() != null) {
            group.setName(groupDTO.getName());
        }
        if (groupDTO.getDescription() != null) {
            group.setDescription(groupDTO.getDescription());
        }

        ContactGroup updatedGroup = contactGroupRepository.save(group);
        return convertToContactGroupDTO(updatedGroup);
    }

    public void deleteGroup(String groupId, String userId) {
        ContactGroup group = contactGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ContactGroup", "id", groupId));

        if (!group.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to delete this group");
        }

        contactGroupRepository.delete(group);
    }

    @Transactional
    public void addContactToGroup(String groupId, String contactId, String userId) {
        ContactGroup group = contactGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ContactGroup", "id", groupId));

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", "id", contactId));

        if (!group.getUserId().equals(userId) || !contact.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to modify this contact or group");
        }

        if (!contact.getGroups().contains(group.getName())) {
            contact.getGroups().add(group.getName());
            contactRepository.save(contact);
            group.setContactCount(group.getContactCount() + 1);
            contactGroupRepository.save(group);
        }
    }

    @Transactional
    public void removeContactFromGroup(String groupId, String contactId, String userId) {
        ContactGroup group = contactGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ContactGroup", "id", groupId));

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", "id", contactId));

        if (!group.getUserId().equals(userId) || !contact.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to modify this contact or group");
        }

        if (contact.getGroups().contains(group.getName())) {
            contact.getGroups().remove(group.getName());
            contactRepository.save(contact);
            group.setContactCount(Math.max(0, group.getContactCount() - 1));
            contactGroupRepository.save(group);
        }
    }

    private ContactDTO convertToContactDTO(Contact contact) {
        ContactDTO dto = new ContactDTO();
        dto.setId(contact.getId());
        dto.setUserId(contact.getUserId());
        dto.setFirstName(contact.getFirstName());
        dto.setLastName(contact.getLastName());
        dto.setEmail(contact.getEmail());
        dto.setPhone(contact.getPhone());
        dto.setGroups(contact.getGroups());
        dto.setActive(contact.isActive());
        return dto;
    }

    private ContactGroupDTO convertToContactGroupDTO(ContactGroup group) {
        ContactGroupDTO dto = new ContactGroupDTO();
        dto.setId(group.getId());
        dto.setUserId(group.getUserId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setContactCount(group.getContactCount());
        return dto;
    }
}
