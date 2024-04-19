package com.example.instagramclone

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class IgViewModelTest {

    private lateinit var viewModel: IgViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var collectionReference: CollectionReference

    @Before
    fun setUp() {
        auth = mockk(relaxed = true)
        db = mockk(relaxed = true)
        collectionReference = mockk(relaxed = true)

        // Provide a mock answer for the FirebaseFirestore.collection("users").document() call
        every { db.collection("users").document(any()) } returns mockk()

        // Provide a mock answer for the FirebaseFirestore.collection("users") call
        every { db.collection("users") } returns collectionReference

        viewModel = IgViewModel(auth, db, mockk())
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }


    @Test
    fun `onLogin with valid email and password`() {
        // Mock successful sign-in
        val email = "parimalborkar3005@gmail.com"
        val password = "Parimal@123"
        val mockedTask: Task<AuthResult> = mockk(relaxed = true) {
            every { isSuccessful } returns true
        }
        every { auth.signInWithEmailAndPassword(email, password) } returns mockedTask

        // Call the function under test
        viewModel.onLogin(email, password)

        // Verify that inProgress is set to false after successful login
        assertEquals(false, viewModel.inProgress.value)

        // Verify that signedIn is set to true after successful login
        assertEquals(true, viewModel.signedIn.value)


    }

    // Add more test cases for edge cases and error scenarios as needed
}
