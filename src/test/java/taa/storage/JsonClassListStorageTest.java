package taa.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import taa.commons.exceptions.DataConversionException;
import taa.model.ClassList;
import taa.model.ReadOnlyTaaData;
import taa.testutil.Assert;
import taa.testutil.TypicalPersons;

public class JsonClassListStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src",
            "test", "data", "JsonClassListStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyTaaData> readAddressBook(String filePath) throws Exception {
        return new JsonTaaStorage(Paths.get(filePath)).readTaaData(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        Assert.assertThrows(DataConversionException.class, ()
                -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataConversionException() {
        Assert.assertThrows(DataConversionException.class, ()
                -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataConversionException() {
        Assert.assertThrows(DataConversionException.class, ()
                -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        ClassList original = TypicalPersons.getTypicalAddressBook();
        JsonTaaStorage jsonAddressBookStorage = new JsonTaaStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveTaaData(original, filePath);
        ReadOnlyTaaData readBack = jsonAddressBookStorage.readTaaData(filePath).get();
        assertEquals(original, new ClassList(readBack));

        // Modify data, overwrite exiting file, and read back
        original.addStudent(TypicalPersons.HOON);
        original.removeStudent(TypicalPersons.ALICE);
        jsonAddressBookStorage.saveTaaData(original, filePath);
        readBack = jsonAddressBookStorage.readTaaData(filePath).get();
        assertEquals(original, new ClassList(readBack));

        // Save and read without specifying file path
        original.addStudent(TypicalPersons.IDA);
        jsonAddressBookStorage.saveTaaData(original); // file path not specified
        readBack = jsonAddressBookStorage.readTaaData().get(); // file path not specified
        assertEquals(original, new ClassList(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, ()
                -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyTaaData addressBook, String filePath) {
        try {
            new JsonTaaStorage(Paths.get(filePath))
                    .saveTaaData(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> saveAddressBook(new ClassList(), null));
    }
}
