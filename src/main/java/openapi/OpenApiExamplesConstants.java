package openapi;

public class OpenApiExamplesConstants {

    public static final String GET_ALL_USERS_SUCCESS = """
            {
                "content": {
                    "elements": [
                        "Barnie",
                        "Bon",
                        "Daniela",
                        "Fred",
                        "Ina",
                        "Ivaylo",
                        "Ivo",
                        "Lola",
                        "Paolo",
                        "Tiana"
                    ],
                    "totalPageCount": 1,
                    "totalElementsCount": 10
                },
                "errors": []
            }
            """;

    public static final String INVALID_USER_NAME_ERROR = """
            {
              "content": null,
              "errors": [
                {
                  "name": "InvalidName",
                  "description": "User name can not be empty."
                }
              ]
            }
            """;

    public static final String DUPLICATE_USER_ERROR = """
            {
              "content": null,
              "errors": [
                {
                  "name": "DuplicateUser",
                  "description": "User 'Fred' is taken."
                }
              ]
            }
            """;

    public static final String DUPLICATE_USER_ERROR_2 = """
            {
              "content": null,
              "errors": [
                {
                  "name": "DuplicateUser",
                  "description": "User 'Bob' is taken."
                }
              ]
            }
            """;

    public static final String INVALID_USER_ERROR = """
            {
              "content": null,
              "errors": [
                {
                  "name": "InvalidUser",
                  "description": "User 'Fred' does not exist."
                }
              ]
            }
            """;

    public static final String INVALID_USER_NAME_ERROR_2 = """
            {
                "content": null,
                "errors": [
                    {
                        "name": "InvalidName",
                        "description": "Current user name same as new user name."
                    }
                ]
            }
            """;

    public static final String PAGE_LESS_THAN_1 = """
            {
                "content": null,
                "errors": [
                    {
                        "name": "IllegalArgument",
                        "description": "Page must be greater than 0"
                    }
                ]
            }
            """;

    public static final String SIZE_LESS_THAN_1 = """
            {
                "content": null,
                "errors": [
                    {
                        "name": "IllegalArgument",
                        "description": "Size must be greater than 0"
                    }
                ]
            }
            """;

    public static final String GET_ALL_TASKS_SUCCESS = """
            {
                "content": {
                    "elements": [
                        {
                            "id": "00000000-0000-0000-0000-000000000001",
                            "title": "Task1",
                            "description": "Task1 description",
                            "priority": "P1",
                            "start_time": "2007-12-03",
                            "deadline": "2008-12-03",
                            "repeat_time": "2009-12-03",
                            "assignees": [
                                "Any"
                            ],
                            "parent_task_id": "00000000-0000-0000-0000-000000000002",
                            "is_completed": false
                        },
                        {
                            "id": "00000000-0000-0000-0000-000000000003",
                            "title": "Task2",
                            "description": "Task2 description",
                            "priority": "P2",
                            "start_time": "2007-12-03",
                            "deadline": "2007-12-31",
                            "repeat_time": "",
                            "assignees": [
                                "Any"
                            ],
                            "parent_task_id": "00000000-0000-0000-0000-000000000004",
                            "is_completed": true
                        }
                    ],
                    "totalPageCount": 2,
                    "totalElementsCount": 2
                },
                "errors": []
            }
            """;
}