import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.nio.file.*;
import java.io.IOException;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Task {
    String value();
}
public class NoteManager {
    private List<Note> notes = new ArrayList<>();
    private Map<String, Note> noteMap = new HashMap<>();
    private static final String FILE_NAME = "notes.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public NoteManager() {
        loadFromFile();
    }

    public void addNote(String text) {
        Note note = new Note(text);
        notes.add(note);
        noteMap.put(text, note);
        System.out.println("Заметка добавлена!");
    }

    public void showAllNotes() {
        if (notes.isEmpty()) {
            System.out.println("Заметок нет.");
            return;
        }

        System.out.println("Все заметки:");
        for (int i = 0; i < notes.size(); i++) {
            System.out.println((i + 1) + ". " + notes.get(i));
        }
    }

    public void deleteNote(int index) {
        if (index < 1 || index > notes.size()) {
            System.out.println("Неверный номер заметки!");
            return;
        }
        Runnable onDelete = new Runnable() {
            public void run() {
                System.out.println("Заметка удалена!");
            }
        };
        Note noteToRemove = notes.get(index - 1);
        notes.remove(index - 1);
        noteMap.remove(noteToRemove.getText());

        onDelete.run();
    }

    @Task("Сохранение заметок в файл")
    public void saveToFile() {
        try {
            List<String> lines = new ArrayList<>();
            for (Note note : notes) {
                lines.add(note.getText() + " | " + note.getCreatedAt().format(formatter));
            }
            Files.write(Paths.get(FILE_NAME), lines);
            System.out.println("Сохранено в " + FILE_NAME);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    public void loadFromFile() {
        try {
            if (!Files.exists(Paths.get(FILE_NAME))) {
                return;
            }

            List<String> lines = Files.readAllLines(Paths.get(FILE_NAME));
            notes.clear();
            noteMap.clear();

            for (String line : lines) {
                String[] parts = line.split(" \\| ");
                if (parts.length == 2) {
                    LocalDateTime createdAt = LocalDateTime.parse(parts[1], formatter);
                    Note note = new Note(parts[0], createdAt);
                    notes.add(note);
                    noteMap.put(parts[0], note);
                }
            }
            System.out.println("Загружено " + notes.size() + " заметок из файла.");
        } catch (IOException e) {
            System.out.println("Ошибка загрузки: " + e.getMessage());
        }
    }

    public void startReminder() {
        new Thread(() -> {
            System.out.println("Напоминание: у вас " + notes.size() + " заметок!");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("Поток прерван: " + e.getMessage());
            }
            System.out.println("Проверка завершена.");
        }).start();
    }

    public void checkAnnotation() {
        try {
            Method method = this.getClass().getMethod("saveToFile");
            if (method.isAnnotationPresent(Task.class)) {
                Task annotation = method.getAnnotation(Task.class);
                System.out.println("Аннотация: " + annotation.value());
            }
        } catch (NoSuchMethodException e) {
            System.out.println("Метод не найден: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        NoteManager manager = new NoteManager();
        Scanner scanner = new Scanner(System.in);

        // Проверка аннотации при запуске
        manager.checkAnnotation();

        while (true) {
            System.out.println("\n=== Менеджер заметок ===");
            System.out.println("1. Добавить заметку");
            System.out.println("2. Показать все заметки");
            System.out.println("3. Удалить заметку по номеру");
            System.out.println("4. Сохранить в файл");
            System.out.println("5. Загрузить из файла");
            System.out.println("6. Запустить напоминание");
            System.out.println("0. Выход");
            System.out.print("Выберите действие: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Введите текст заметки: ");
                        String text = scanner.nextLine();
                        manager.addNote(text);
                        break;
                    case 2:
                        manager.showAllNotes();
                        break;
                    case 3:
                        System.out.print("Введите номер заметки для удаления: ");
                        int index = scanner.nextInt();
                        manager.deleteNote(index);
                        break;
                    case 4:
                        manager.saveToFile();
                        break;
                    case 5:
                        manager.loadFromFile();
                        break;
                    case 6:
                        manager.startReminder();
                        break;
                    case 0:
                        System.out.println("Выход из программы.");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Неверный выбор!");
                }
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: введите число!");
                scanner.nextLine();
            }
        }
    }
}