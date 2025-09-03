//package com.recruitment.controller;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import com.recruitment.entity.Bookmark;
//import com.recruitment.entity.Job;
//import com.recruitment.entity.User;
//import com.recruitment.repository.BookmarkRepository;
//import com.recruitment.repository.JobRepository;
//import com.recruitment.repository.UserRepo;
//
//@RestController
//@RequestMapping("/api/bookmarks")
//@CrossOrigin(origins = "http://backend-n4w7.onrender.com", allowCredentials = "true")
//public class BookmarkController {
//
//    @Autowired
//    private BookmarkRepository bookmarkRepo;
//
//    @Autowired
//    private JobRepository jobRepo;
//
//    @Autowired
//    private UserRepo userRepo;
//
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<String>> getBookmarkedJobs(@PathVariable String userId) {
//        List<Bookmark> bookmarks = bookmarkRepo.findByUser_UserId(userId);
//        List<String> jobIds = bookmarks.stream()
//                .map(b -> b.getJob().getJobId())
//                .toList();
//        return ResponseEntity.ok(jobIds);
//    }
//
//    @PostMapping
//    public ResponseEntity<?> addBookmark(@RequestBody Map<String, String> body) {
//        String userId = body.get("userId");
//        String jobId = body.get("jobId");
//
//        if (bookmarkRepo.existsByUser_UserIdAndJob_JobId(userId, jobId)) {
//            return ResponseEntity.badRequest().body("Already bookmarked");
//        }
//
//        Optional<Job> jobOpt = jobRepo.findByJobId(jobId);
//        if (jobOpt.isEmpty()) {
//            return ResponseEntity.status(404).body("Job not found");
//        }
//
//        Optional<User> userOpt = userRepo.findByUserId(userId);
//        if (userOpt.isEmpty()) {
//            return ResponseEntity.status(404).body("User not found");
//        }
//
//        Bookmark bookmark = new Bookmark();
//        bookmark.setUser(userOpt.get());
//        bookmark.setJob(jobOpt.get());
//        bookmarkRepo.save(bookmark);
//
//        return ResponseEntity.ok("Bookmark added");
//    }
//
//    @DeleteMapping("/{userId}/{jobId}")
//    public ResponseEntity<?> removeBookmark(@PathVariable String userId, @PathVariable String jobId) {
//        if (!bookmarkRepo.existsByUser_UserIdAndJob_JobId(userId, jobId)) {
//            return ResponseEntity.status(404).body("Bookmark not found");
//        }
//        bookmarkRepo.deleteByUser_UserIdAndJob_JobId(userId, jobId);
//        return ResponseEntity.ok("Bookmark removed");
//    }
//}
