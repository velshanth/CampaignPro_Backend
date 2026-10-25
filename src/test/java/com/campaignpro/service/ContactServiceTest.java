package com.campaignpro.service;

import com.campaignpro.dto.ContactDTO;
import com.campaignpro.exception.ResourceNotFoundException;
import com.campaignpro.model.Contact;
import com.campaignpro.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    private Contact testContact;
    private ContactDTO contactDTO;

    @BeforeEach
    void setUp() {
        testContact = new Contact();
        testContact.setId("contact1");
        testContact.setUserId("user1");
        testContact.setFirstName("John");
        testContact.setLastName("Doe");
        testContact.setEmail("john@example.com");
        testContact.setPhone("1234567890");
        testContact.setActive(true);

        contactDTO = new ContactDTO();
        contactDTO.setFirstName("John");
        contactDTO.setLastName("Doe");
        contactDTO.setEmail("john@example.com");
        contactDTO.setPhone("1234567890");
    }

    @Test
    void testCreateContactSuccess() {
        when(contactRepository.save(any(Contact.class))).thenReturn(testContact);

        ContactDTO result = contactService.createContact("user1", contactDTO);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    void testGetAllContactsSuccess() {
        when(contactRepository.findByUserId("user1")).thenReturn(Arrays.asList(testContact));

        List<ContactDTO> result = contactService.getAllContacts("user1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
    }

    @Test
    void testGetContactByIdSuccess() {
        when(contactRepository.findById("contact1")).thenReturn(Optional.of(testContact));

        ContactDTO result = contactService.getContactById("contact1", "user1");

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
    }

    @Test
    void testGetContactByIdNotFound() {
        when(contactRepository.findById("contact1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            contactService.getContactById("contact1", "user1");
        });
    }

    @Test
    void testUpdateContactSuccess() {
        when(contactRepository.findById("contact1")).thenReturn(Optional.of(testContact));
        when(contactRepository.save(any(Contact.class))).thenReturn(testContact);

        ContactDTO result = contactService.updateContact("contact1", "user1", contactDTO);

        assertNotNull(result);
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    void testDeleteContactSuccess() {
        when(contactRepository.findById("contact1")).thenReturn(Optional.of(testContact));

        contactService.deleteContact("contact1", "user1");

        verify(contactRepository, times(1)).delete(testContact);
    }

    @Test
    void testDeleteContactNotFound() {
        when(contactRepository.findById("contact1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            contactService.deleteContact("contact1", "user1");
        });
    }
}
