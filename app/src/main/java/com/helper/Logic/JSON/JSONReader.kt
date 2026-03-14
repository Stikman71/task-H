package com.helper.Logic.JSON

import android.util.Log

object PathBuilder {
    /**
     * Формирует путь для задачи.
     *
     * @param pathParts Массив строк, представляющий части пути:
     *                  - [0] "Tasks" (папка, которая хранит задачи)
     *                  - [1] код языка (например, "en", "ru")
     *                  - [2] класс в формате "Class_#", где # — цифра (например, "Class_1")
     *                  - [3] тип задания (например, "Task", "Quiz")
     *                  - [4] имя файла с расширением .json
     * @return Сформированный путь
     * @throws IllegalArgumentException Если один из параметров не соответствует ожидаемому формату
     */
    fun buildPathForTask(pathParts: Array<String>): String {
        // Проверяем, что массив pathParts состоит из 5 частей
        if (pathParts.size != 5) {
            Log.d("PathBuilder", "Неверное количество элементов в пути. Ожидается 5 частей.")
            throw IllegalArgumentException("Неверное количество элементов в пути. Ожидается 5 частей.")
        }

        Log.d("PathBuilder", "Полученные части пути: ${pathParts.joinToString(", ")}")

        // Проверка, что первая часть — это "Tasks"
        if (pathParts[0] != "Tasks") {
            Log.d("PathBuilder", "Первая часть пути не 'Tasks': ${pathParts[0]}")
            throw IllegalArgumentException("Первая часть пути должна быть 'Tasks'.")
        }

        // Проверка, что вторая часть пути является языковым кодом (например, en, ru)
        val languageCode = pathParts[1]
        Log.d("PathBuilder", "Языковой код: $languageCode")
        if (!languageCode.matches(Regex("^[a-z]{2}$"))) {
            Log.d("PathBuilder", "Неверный языковой код: $languageCode")
            throw IllegalArgumentException("Вторая часть пути должна быть языковым кодом в формате 'en', 'ru'.")
        }

        // Проверка, что третья часть пути соответствует паттерну Class_*
        val classPattern = Regex("^Class_\\d+$")
        Log.d("PathBuilder", "Класс: ${pathParts[2]}")
        if (!pathParts[2].matches(classPattern)) {
            Log.d("PathBuilder", "Неверный формат класса: ${pathParts[2]}")
            throw IllegalArgumentException("Третья часть пути должна соответствовать паттерну 'Class_номер'. Пример: 'Class_1'.")
        }

        // Проверка, что четвертая часть пути является типом задания (например, "Task", "Quiz")
        val taskType = pathParts[3].trim() // Убираем пробелы с обеих сторон
        Log.d("PathBuilder", "Тип задания: $taskType")
        if (taskType.isEmpty() || taskType[0].isLowerCase()) {
            Log.d("PathBuilder", "Неверный тип задания: $taskType")
            throw IllegalArgumentException("Четвертая часть пути должна быть корректным типом задания (например, 'Task' или 'Quiz').")
        }

        // Проверка, что пятая часть пути имеет расширение .json
        val fileName = pathParts[4]
        Log.d("PathBuilder", "Имя файла: $fileName")
        if (!fileName.endsWith(".json")) {
            Log.d("PathBuilder", "Неверное расширение файла: $fileName")
            throw IllegalArgumentException("Пятая часть пути должна быть файлом с расширением '.json'.")
        }

        // Формируем путь с добавлением "Tasks" в начало
        val fullPath = pathParts.joinToString("/")

        Log.d("PathBuilder", "Сформированный путь: $fullPath")
        return fullPath
    }
}

fun sanitizeTopicName(topicName: String): String {
    // Словарь для транслитерации с кириллицы на латиницу
    val translitMap = mapOf(
        'А' to "A", 'Б' to "B", 'В' to "V", 'Г' to "G", 'Д' to "D", 'Е' to "E", 'Ё' to "Yo", 'Ж' to "Zh",
        'З' to "Z", 'И' to "I", 'Й' to "J", 'К' to "K", 'Л' to "L", 'М' to "M", 'Н' to "N", 'О' to "O",
        'П' to "P", 'Р' to "R", 'С' to "S", 'Т' to "T", 'У' to "U", 'Ф' to "F", 'Х' to "H", 'Ц' to "Ts",
        'Ч' to "Ch", 'Ш' to "Sh", 'Щ' to "Sch", 'Ъ' to "Y", 'Ы' to "Y", 'Ь' to "'", 'Э' to "E", 'Ю' to "Yu",
        'Я' to "Ya",
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'е' to "e", 'ё' to "yo", 'ж' to "zh",
        'з' to "z", 'и' to "i", 'й' to "j", 'к' to "k", 'л' to "l", 'м' to "m", 'н' to "n", 'о' to "o",
        'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t", 'у' to "u", 'ф' to "f", 'х' to "h", 'ц' to "ts",
        'ч' to "ch", 'ш' to "sh", 'щ' to "sch", 'ъ' to "y", 'ы' to "y", 'ь' to "'", 'э' to "e", 'ю' to "yu",
        'я' to "ya"
    )

    // Трансляция каждого символа в строке
    val translated = topicName.map { char ->
        translitMap[char] ?: char.toString()  // Если символ не найден в словаре, оставляем как есть
    }.joinToString("")

    // Заменяем недопустимые символы на безопасные
    return translated
        .replace("/", "_")   // Заменяем "/" на "_"
        .replace("\"", "'")   // Заменяем '"' на "'"
        .replace(":", "_")    // Заменяем ":" на "_"
        .replace("|", "_")    // Заменяем "|" на "_"
        .replace("?", "_")    // Заменяем "?" на "_"
        .replace("<", "_")    // Заменяем "<" на "_"
        .replace(">", "_")    // Заменяем ">" на "_"
        .replace(" ", "_")    // Заменяем пробелы на "_"
}