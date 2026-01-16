package com.ptit.library.service;

import com.ptit.library.model.Book;
import com.ptit.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    public List<Book> searchBooks(String keyword, String filter1, String filter2, String filter3) {
        List<Book> books = bookRepository.searchByKeyword(keyword);
        
        // Apply sorting based on filters
        books = books.stream()
            .sorted((b1, b2) -> {
                // Filter 1: newest
                if ("newest".equals(filter1) && !b1.getPublishedYear().equals(b2.getPublishedYear())) {
                    return -b1.getPublishedYear().compareTo(b2.getPublishedYear());
                }
                
                // Filter 2: quantity
                if ("quantity".equals(filter2) && !b1.getCopiesAvailable().equals(b2.getCopiesAvailable())) {
                   
                    return -b1.getCopiesAvailable().compareTo(b2.getCopiesAvailable());
                }
                
                // Filter 3: most borrowed
                if ("mostBorrowed".equals(filter3)) {
                    Integer borrowNum1 = b1.getTotalCopies() - b1.getCopiesAvailable();
                    Integer borrowNum2 = b2.getTotalCopies() - b2.getCopiesAvailable();
                    if (!borrowNum1.equals(borrowNum2)) {
                        return -borrowNum1.compareTo(borrowNum2);
                    }
                }
                
                return b1.getAuthor().compareTo(b2.getAuthor());
            })
            .collect(Collectors.toList());
        
        return books;
    }
    
    public Optional<Book> findById(Integer id) {
        return bookRepository.findById(id);
    }
    
    public List<Book> findAll() {
        return bookRepository.findAll();
    }
}
