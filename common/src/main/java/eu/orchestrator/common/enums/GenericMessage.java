package eu.orchestrator.common.enums;

import eu.orchestrator.common.util.LocaleUtil;

import jakarta.servlet.http.HttpServletRequest;

public enum GenericMessage {

    GENERIC_SUCCESS("Request successfully executed", "Το αίτημα εκτελέστηκε επιτυχώς", "1"),
    GENERIC_ERROR("Error occurred! Please try again!", "Προέκυψε σφάλμα! Δοκιμάστε ξανά!", "0"),
    NOT_AUTHORIZED("Access Denied", "Η πρόσβαση δεν επιτρέπεται", "2"),
    NOT_FOUND("Entity not found", "Entity not found", "3"),
    REQUIRED_FIELDS_MISSING("Please fill in all the required fields",
            "Παρακαλώ συμπληρώστε όλα τα υποχρεωτικά πεδία", "4"),
    REQUIRED_FIELDS_WITH_PROPER_VALUES("Please fill in all fields with propper values",
            "Παρακαλώ συμπληρώστε όλα τα πεδία με σωστες τιμες", "4"),
    GENERIC_CHART_FETCHED("Chart has been fetched successfully", "Το γράφημα λήφθηκε επιτυχώς", "5"),


    // Country
    COUNTRY_NOT_EXIST("The country doesn't exist", "Η χώρα δεν υπάρχει!", "5"),

    PROVIDER_TYPE_NOT_EXIST("The provider type doesn't exist", "Ο τύπος του παρόχου δεν υπάρχει!", "6"),

    // Volume
    ERROR_ON_VOLUME_MAPPING("The host path does exist on yours workspace OR the path is not directory",
            "Λαθος στο mapping των volume", "5"),


    //FIXED
    ORGANIZATION_FETCHED("Organization has been fetched successfully",
            "Ο οργανισμός λήφθηκε επιτυχώς", "5"),
    ORGANIZATION_ALREADY_EXISTS("An organization with this name already exists",
            "Οργανισμός με αυτό το όνομα υπάρχει ήδη", "5"),
    ORGANIZATION_NOT_EXISTS("An organization doesn't already exists",
            "Οργανισμός με αυτό το δεν υπάρχει ", "5"),
    USER_ASSIGNED_TO_ORGANIZATION("This user is already assigned to an organization",
            "Ο χρήστης ανήκει ήδη σ' έναν οργανισμό", "5"),
    ORGANIZATION_HAS_COMPONENTS(
            "This organization has registered components. Please remove them first!",
            "Ο οργανισμός έχει καταχωρημένα components. Παρακαλώ αφαιρέστε τα πρώτα!", "8"),
    ORGANIZATION_HAS_APPLICATIONS(
            "This organization has registered applications. Please remove them first!",
            "Ο οργανισμός έχει καταχωρημένες εφαρμογές. Παρακαλώ αφαιρέστε 'τες πρώτα!", "9"),
    ORGANIZATION_HAS_APPLICATION_INSTANCES(
            "This organization has registered application instances. Please remove them first!",
            "Ο οργανισμός έχει ενεργές εφαρμογές. Παρακαλώ αφαιρέστε 'τες πρώτα!", "10"),
    ORGANIZATION_HAS_PROVIDERS(
            "This organization has registered providers. Please remove them first!",
            "Ο χρήστης οργανισμός καταχωρημένα resources. Παρακαλώ αφαιρέστε τα πρώτα!", "11"),
    ORGANIZATION_HAS_SSH_KEYS("This organization has registered SSH keys. Please remove them first!",
            "Ο χρήστης οργανισμός καταχωρημένα κλειδιά. Παρακαλώ αφαιρέστε τα πρώτα!", "11"),
    NO_ORGANIZATION("There isn't an organization field!", "Δεν υπάρχει το πεδίου οργανισμός!", "12"),
    ORGANIZATION_NOT_ASSIGN("You have no access add person on with organization",
            "Δεν έχετε πρόσβαση σε αυτό τον οργανισμό για να προσθέσετε άτομα", "30"),
    ORGANIZATION_NOT_AUTHORIZED("You have no access to this organization",
            "Δεν έχετε πρόσβαση σε αυτό τον οργανισμό", "30"),
    ORGANIZATION_CANNOT_DELETED("You cannot delete Admin_Organization",
            "Δεν έχετε πρόσβαση σε αυτό τον οργανισμό, Οργανισμός_Διαχειριστή", "31"),
    ORGANIZATION_CANNOT_EDIT("You cannot edit Admin_Organization",
            "Δεν έχετε πρόσβαση σε αυτό τον οργανισμό, Οργανισμός_Διαχειριστή", "32"),

    //FIXED
    USER_FETCHED("User has been fetched successfully", "Ο χρήστης λήφθηκε επιτυχώς", "5"),
    USERNAME_ALREADY_EXISTS("A user with this username already exists",
            "Χρήστης με αυτό το όνομα υπάρχει ήδη", "5"),
    EMAIL_ALREADY_EXISTS("A user with this email already exists",
            "Χρήστης με αυτό το ηλεκτρονικό ταχυδρομείο υπάρχει ήδη", "6"),
    EMAIL_INVALID_FORMAT("An invalid email has been entered. Please try again!",
            "Το ηλ. ταχυδρομείο δεν είναι σε έγκυρη μορφή. Παρακαλώ δοκιμάστε ξανά!", "7"),
    USER_HAS_COMPONENTS("This user has registered components. Please remove them first!",
            "Ο χρήστης έχει καταχωρημένα components. Παρακαλώ αφαιρέστε τα πρώτα!", "8"),
    USER_HAS_APPLICATIONS("This user has registered applications. Please remove them first!",
            "Ο χρήστης έχει καταχωρημένες εφαρμογές. Παρακαλώ αφαιρέστε 'τες πρώτα!", "9"),
    USER_HAS_APPLICATION_INSTANCES(
            "This user has registered application instances. Please remove them first!",
            "Ο χρήστης έχει ενεργές εφαρμογές. Παρακαλώ αφαιρέστε 'τες πρώτα!", "10"),
    USER_HAS_PROVIDERS("This user has registered providers. Please remove them first!",
            "Ο χρήστης έχει καταχωρημένα resources. Παρακαλώ αφαιρέστε τα πρώτα!", "11"),
    USER_HAS_SSH_KEYS("This user has registered SSH keys. Please remove them first!",
            "Ο χρήστης έχει καταχωρημένα κλειδιά. Παρακαλώ αφαιρέστε τα πρώτα!", "11"),
    USER_ACTIVATED("User has been activated successfully", "Ο χρήστης ενεργοποιήθηκε επιτυχώς", "12"),
    USER_DEACTIVATED("User has been deactivated successfully", "Ο χρήστης απενεργοποιήθηκε επιτυχώς",
            "13"),
    USER_LOGOUT("User has been logged out successfully", "Ο χρήστης αποσυνδέθηκε επιτυχώς", "14"),
    USER_REGISTERED("User has been registered successfully", "Ο χρήστης εγγράφηκε επιτυχώς", "15"),
    USER_AUTHENTICATED_EXISTS("Authenticated user exists", "Υπάρχει συνδεδεμένος χρήστης", "16"),
    USER_PASSWORD_NO_MATCH("Your password and confirmation password do not match",
            "Ο κωδικός πρόσβασης δεν επιβεβαιώνεται", "17"),
    USER_CHANGE_ROLE_OR_ORGANIZATION("User's  role and organization not change",
            "Ο ρόλος και ο ογρανισμός του χρήστη δεν μπορει να αλλάξη", "18"),
    USER_CHANGE_ROLE_NOT_EXIST("User's  role not exist",
            "Ο ρόλος δεν ύπάρχει", "19"),
    USER_NOT_AUTHORIZED("You have no access to users", "Δεν έχετε πρόσβαση στους χρήστες", "15"),


    //FIXED
    QI_ALREADY_EXISTS("A QI with this value already exists", "Υπηρεσία με αυτή την κλάση υπάρχει ήδη",
            "17"),
    QI_USED("This QI is used in constraints of access interfaces and cannot be deleted",
            "Η υπηρεσία αυτή χρησιμοποιείται και δεν μπορεί να διαγραφεί", "18"),
    QI_FETCHED("QI has been fetched successfully", "Η υπηρεσία λήφθηκε επιτυχώς", "19"),
    QI_NOT_AUTHORIZED("You have no access on QI", "Δεν έχετε πρόσβαση σε αυτή την υπηρεσία", "18"),
    QI_NOT_EXIST("This QI doesn't exist", "Αυτή την υπηρεσία δεν υπάρχει", "18"),

    //FIXED
    RADIO_SERVICE_TYPE_ALREADY_EXISTS("A radio service type with this SST value already exists",
            "Υπηρεσία με αυτό το SST υπάρχει ήδη", "21"),
    RADIO_SERVICE_TYPE_USED(
            "This radio service type is used in constraints of access interfaces and cannot be deleted",
            "Η υπηρεσία αυτή χρησιμοποιείται και δεν μπορεί να διαγραφεί", "22"),
    RADIO_SERVICE_TYPE_FETCHED("Radio service type has been fetched successfully",
            "Η υπηρεσία λήφθηκε επιτυχώς", "20"),
    RADIO_SERVICE_NOT_AUTHORIZED("You have no access on radio service",
            "Δεν έχετε πρόσβαση σε αυτή την υπηρεσία", "21"),
    RADIO_SERVICE_NOT_EXIST("This radio service type  doesn't exist", "Αυτή την υπηρεσία δεν υπάρχει",
            "21"),

    //FIXED
    SSH_KEY_ALREADY_EXISTS("An ssh key with this name already exists",
            "Κλειδί μ' αυτό το όνομα υπάρχει ήδη", "24"),
    SSH_KEY_USED("This ssh key is used to connect to a component node instance and cannot be deleted",
            "Το κλειδί αυτό χρησιμοποιείται και δεν μπορεί να διαγραφεί", "26"),
    SSH_KEY_FETCHED("Ssh key has been fetched successfully", "Το κλειδί λήφθηκε επιτυχώς", "23"),
    SSH_KEY_DEFAULT("This ssh key is the default one and cannot be deleted",
            "Το κλειδί αυτό είναι το προεπιλεγμένο και δεν μπορεί να διαγραφεί", "25"),
    SSH_KEY_NOT_AUTHORIZED("You have no access on this ssh key",
            "Δεν έχετε πρόσβαση σε αυτό το κλειδί", "26"),
    SSH_KEY_NOT_EXIST("This SSH key doesn't exist", "Αυτό τo κλειδί δεν υπάρχει", "26"),


    PLUGIN_FETCHED("Plugin has been fetched successfully", "Το plugin λήφθηκε επιτυχώς", "27"),
    PLUGIN_ALREADY_EXISTS("A plugin with this name already exists",
            "Ένα plugin μ' αυτό το όνομα υπάρχει ήδη", "28"),
    PLUGIN_USED_IN_COMPONENTS("This plugin is used by components",
            "Αυτό το plugin χρησιμοποιείται σε components", "29"),
    DEFAULT_PLUGIN("This is a default/immutable plugin. It cannot be deleted!",
            "Αυτό είναι ένα προεπιλεγμένο plugin. Δεν μπορεί να διαγραφεί!", "30"),
    DEFAULT_PLUGIN_NOT_AUTHORIZED("Not authorized to define default plugin!",
            "Δεν επιτρέπεται να οριστεί προεπιλεγμενο plugin!", "31"),
    IMMUTABLE_PLUGIN_NOT_AUTHORIZED("Not authorized to define immutable plugin!",
            "Δεν επιτρέπεται να οριστεί αμετάβλητο plugin!", "32"),
    PUBLIC_PLUGIN_NOT_AUTHORIZED("Not authorized to define public plugin!",
            "Δεν επιτρέπεται να οριστεί δημόσιο plugin!", "33"),
    PLUGIN_FETCH_NOT_ALLOWED("You are not allowed to get access to this plugin!",
            "Δεν επιτρέπεται η πρόσβαση σε αυτό το plugin!", "34"),

    //FIXED
    ID_RULE_SET_FETCHED("ID rule set has been fetched successfully", "Ο κανόνας λήφθηκε επιτυχώς",
            "30"),
    ID_RULE_SET_ALREADY_EXISTS("An ID rule set with this name already exists",
            "Ένας κανόνας μ' αυτό το όνομα υπάρχει ήδη", "28"),
    ID_RULE_SET_USED_IN_COMPONENT_NODE_INSTANCE("This ID rule set is used by application instances",
            "Αυτός ο κανόνας χρησιμοποιείται ήδη σε εφαρμογές", "30"),
    ID_RULE_SET_CANNOT_DEFINE_AS_PUBLIC("You have no permission to set a ID rule set as public",
            "Αυτός ο κανόνας δεν μπορεί να οριστεί σαν public", "30"),
    ID_RULE_SET_NOT_AUTHORIZED("You have no access this ID rule set",
            "Δεν έχετε πρόσβαση σε αυτό το κανόνα", "30"),

    PROVIDER_TYPE_FETCHED("Provider's type has been fetched successfully",
            "Ο τύπος παρόχου λήφθηκε επιτυχώς", "30"),
    PROVIDER_CREDENTIALS_SUCCESS("Provider's credentials are successfully",
            "Ο τα στοιχεια του παρόχου είναι σώστα ", "30"),
    PROVIDER_CREDENTIALS_FAIL("Provider's credentials are not valid",
            "Ο τα στοιχεια του παρόχου είναι λαθος ", "30"),
    REGION_FETCHED("Region has been fetched successfully", "Η περιοχή λήφθηκε επιτυχώς", "30"),

    PROVIDER_FETCHED("Provider has been fetched successfully", "Ο πάροχος λήφθηκε επιτυχώς", "30"),
    PROVIDER_ALREADY_EXISTS("A provider with this name already exists",
            "Ένας πάροχος μ' αυτό το όνομα υπάρχει ήδη", "31"),
    PROVIDER_NOT_AUTHORIZED("You have no access to Provider",
            "Δεν έχετε πρόσβαση σε αυτό τον πάροχος", "30"),
    PROVIDER_NOT_REGION_SET("You have no region for this Provider",
            "Δεν έχετε ορίζει χώρα σε αυτό τον πάροχος", "30"),
    DEFAULT_PROVIDER_MISSING("A default provider is missing", "Ένας προεπιλεγμένος πάροχος λείπει",
            "32"),
    PROVIDER_USED_IN_APPLICATION_INSTANCES(
            "This provider is used by application instances. Please remove them first!",
            "Αυτός ο πάροχος χρησιμοποιείται ήδη σε ενεργές εφαρμογές. Παρακαλώ αφαιρέστε 'τες πρώτα!",
            "33"),
    CHANGE_DEFAULT_PROVIDER("This is the default provider. Please choose another one first!",
            "Αυτός είναι ο προεπιλεγμένος πάροχος. Παρακαλώ επιλέξτε πρώτα έναν άλλον!", "34"),

    NEW_PASSWORD_MISMATCH("Passwords are not the same",
            "Ο νέος κωδικός δεν είναι ταυτίζεται και στα δύο πεδία", "35"),
    PASSWORD_POLICY_REQUIREMENTS("Password doesn't meet the policy requirements",
            "Ο κωδικός δεν πληρεί τις προδιαγραφές ασφαλείας", "35"),
    CURRENT_PASSWORD_INCORRECT("Current password is incorrect", "Ο τρέχων κωδικός δεν είναι σωστός",
            "36"),
    PASSWORD_TOO_SHORT("Password is too short. Please use at least four characters",
            "Ο κωδικός είναι πολύ σύντομος. Παρακαλώ χρησιμοποιήστε τουλάχιστον 4 χαρακτήρες!", "37"),
    USERNAME_TOO_SHORT("Username is too short. Please use at least four characters",
            "Το όνομα χρήστη είναι πολύ σύντομο. Παρακαλώ χρησιμοποιήστε τουλάχιστον 4 χαρακτήρες!",
            "38"),

    DOCKER_CREDENTIALS_VALID("Docker credentials are valid", "Σωστά Docker διαπιστευτήρια", "30"),
    DOCKER_CREDENTIALS_INVALID("Docker credentials are not response", "Λάθος Docker διαπιστευτήρια", "30"),

    //Domain
    DOMAIN_SERVICE_DELETION_FAILED("Domain does not deleted successfully", "Σφάλμα το όνομα δεν έσβησε", "33"),
    DOMAIN_DELETED("Domain as been deleted successfully", "Το ονόμα έσβησε", "33"),
    DOMAIN_DOES_NOT_EXIST("Domain doesn't exist", "Το ονόμα δεν υπάρχει ", "33"),

    DOMAIN_CREATED("Domains has been added successfully", "Ονόματα created", "31"),
    DOMAIN_UPDATED("Domain has been updated successfully", "Ονόματα created", "31"),
    DOMAIN_SERVICE_CREATION_FAILED("Domain does not register successfully", "Ονόματα λήφθηκαν επιτυχώς", "33"),
    DOMAIN_ALREADY_EXIST("Domains already exist", "Ονόματα υπάρχει", "32"),


    SUFFIX_DOMAIN_FETCHED("Available suffix domains has been fetched successfully", "Ονόματα λήφθηκαν επιτυχώς", "31"),

    COMPONENT_FETCHED("Component has been fetched successfully", "Component λήφθηκε επιτυχώς", "30"),
    COMPONENT_HAS_ITS_OWN_INTERFACE_AS_REQUIRED("Component has is own interface as required",
            "Component εχει σαν προαπαιτούμενο το δικό του interface", "30"),
    COMPONENT_CANDIDATES_FETCHED("Component's candidates have been fetched successfully",
            "Component's candidates λήφθηκαν επιτυχώς", "30"),
    COMPONENT_ALREADY_EXISTS("A component with this name already exists",
            "Component με αυτό το όνομα υπάρχει ήδη", "39"),
    COMPONENT_HAS_APPLICATIONS("This component is used by applications. Please remove them first!",
            "Αυτό το component χρησιμοποιείται ήδη σε εφαρμογές. Παρακαλώ αφαιρέστε 'τες πρώτα!", "40"),
    COMPONENT_EXPOSED_INTERFACE_IS_USING("This component's interface is used by other component as required. Please remove them first!",
            "Αυτό το interface του component χρησιμοποιείται απο κάποιο αλλο component. Παρακαλώ αφαιρέστε 'τες πρώτα!", "40"),
    COMPONENT_REQUIREMENT_ALREADY_EXISTS("Requirements for this component already exists",
            "Απαιτήσεις για αυτό το component υπάρχουν ήδη", "41"),
    COMPONENT_FETCH_NOT_ALLOWED("You are not allowed to get access to this component!",
            "Δεν επιτρέπεται η πρόσβαση σε αυτό το component!", "42"),
    PUBLIC_COMPONENT_NOT_AUTHORIZED("Not authorized to define public component!",
            "Δεν επιτρέπεται να οριστεί δημόσιο component!", "42"),
    COMPONENT_NODE_MUST_BE_UNIQUE("Component node names must be unique",
            "Τα ονόματα των component nodes πρέπει να είναι μοναδικά", "42"),

    COMPONENT_UPDATE_EXCEPT_INTERFACES("Component's interface NOT change because the component is used from application",
            "Τα interface του component δεν άλλαξαν διοτι το component χρησιμοποίται", "42"),

    INTERFACE_ALREADY_EXISTS("An interface with this name already exists",
            "Διεπαφή με αυτό το όνομα υπάρχει ήδη", "39"),

    INTERFACE_OBJECT_NULL("Not provided interface object, or object is null",
            "Δεν δόθηκε αντικείμενο interface, η το αντικείμενο είναι κενό", "79"),

    COMPONENT_HEALTH_CHECK_ALREADY_EXISTS("Health check for this component already exists",
            "Health check για αυτό το component υπάρχουν ήδη", "43"),
    COMPONENT_NODE_INSTANCE_HEALTH_CHECK_INSTANCE_ALREADY_EXISTS(
            "Health check instance for this component node instance already exists",
            "Health check instance για αυτό το component node instance υπάρχει ήδη", "44"),

    FLAVOR_LOWER_THAN_MINIMUM_EXECUTION_REQUIREMENTS(
            "Flavor for this component node instance must be equal or greater than minimum execution requirements",
            "Απαιτήσεις για αυτό το component node instance υπάρχουν ήδη", "45"),
    MINIMUM_WORKERS_GREATER_THAN_MAXIMUM_WORKERS("Minimum Workers must be lower than maximum workers",
            "Παρακαλώ συμπληρώστε όλα τα υποχρεωτικά πεδία", "46"),
    COMPONENT_NODE_INSTANCE_ALREADY_EXISTS("A component node instance with this name already exists",
            "Component node instance με αυτό το όνομα υπάρχει ήδη", "47"),
    COMPONENT_NODE_IS_NOT_EXIST("A component node doesn't exist",
            "Component node δεν υπάρχει", "47"),
    COMPONENT_NODE_INSTANCE_NOT_EXIST("A component node instance doesn't exist",
            "Component node instance δεν υπάρχει", "47"),
    COMPONENT_NODE_INSTANCE_SCALE_UP("This component node instance has been scaled up",
            "This component node instance has been scaled up", "48"),
    COMPONENT_NODE_INSTANCE_SCALE_DOWN("This component node instance has been scaled down",
            "This component node instance has been scaled down", "49"),
    COMPONENT_NODE_INSTANCE_FLAVOR_ALREADY_EXISTS(
            "Flavor for this component node instance already exists",
            "Απαιτήσεις για αυτό το component node instance υπάρχουν ήδη", "50"),
    COMPONENT_NODE_INSTANCE_LOCATION_ALREADY_EXISTS(
            "Location for this component node instance already exists",
            "Τοποθεσία για αυτό το component node instance υπάρχουν ήδη", "51"),
    COMPONENT_NODE_INSTANCE_FETCHED("Component node instance has been fetched successfully",
            "Το instance λήφθηκε επιτυχώς", "23"),
    COMPONENT_NODE_INSTANCE_IS_NOT_EXISTS("A component node instance doesn't exist",
            "Component node instance δεν υπάρχει", "75"),
    COMPONENT_NODE_INSTANCE_IP_IS_NOT_EXISTS("A component node instance ip doesn't exist",
            "Component node instance ip δεν υπάρχει", "76"),
    COMPONENT_NODE_INSTANCE_AFFINITY_CREATED("Component node instance affinity has been created",
            "Component node instance affinity δημιουργήθηκε", "100"),
    COMPONENT_NODE_INSTANCE_AFFINITY_NOT_EXISTS("Component node instance affinity  does not exists",
            "Component node instance affinity δεν υπάρχει", "101"),
    COMPONENT_OBJECT_NULL("Not provided component object, or object is null",
            "Δεν δόθηκε αντικείμενο component, η το αντικείμενο είναι κενό", "78"),

    COMPONENT_ID_NULL("Not provided component id, or id is null",
            "Δεν δόθηκε id για το component, ή το id είναι κενό", "80"),
    APPLICATION_ALREADY_EXISTS("An application with this name already exists",
            "Εφαρμογή με αυτό το όνομα υπάρχει ήδη", "52"),
    APPLICATION_USED_IN_APPLICATION_INSTANCES(
            "This application is used by application instances. Please remove them first!",
            "Αυτό η εφαρμογή χρησιμοποιείται ήδη σε ενεργές εφαρμογές. Παρακαλώ αφαιρέστε 'τες πρώτα!",
            "53"),
    APPLICATION_NOT_AUTHORIZED("You have no access on this Application",
            "Δεν έχετε πρόσβαση σε αυτήν την εφαρμογή", "26"),
    APPLICATION_NOT_EXIST("This Application doesn't exist", "Αυτή η εφαρμογή δεν υπάρχει", "26"),
    APPLICATION_FETCHED("Application has been fetched successfully", "Η εφαρμογή λήφθηκε επιτυχώς",
            "23"),
    APPLICATION_OBJECT_NULL("Not provided application object, or object is null",
            "Δεν δόθηκε αντικείμενο application, η το αντικείμενο είναι κενό", "81"),
    PUBLIC_APPLICATION_NOT_AUTHORIZED("Not authorized to define public application!",
            "Δεν επιτρέπεται να οριστεί δημόσιο application!", "42"),

    APPLICATION_INSTANCE_DEPLOYMENT_STARTED("An application instance is deploying",
            "Ενεργή εφαρμογή με αυτό το όνομα υπάρχει ήδη", "54"),
    APPLICATION_INSTANCE_ALREADY_EXISTS("An application instance with this name already exists",
            "Ενεργή εφαρμογή με αυτό το όνομα υπάρχει ήδη", "54"),
    APPLICATION_INSTANCE_NOT_EXIST("This Application Instance doesn't exist",
            "Αυτό το Instance δεν υπάρχει", "26"),
    APPLICATION_INSTANCE_FETCHED("Application Instance has been fetched successfully",
            "Το Instance λήφθηκε επιτυχώς", "23"),
    APPLICATION_INSTANCE_NOT_AUTHORIZED("You have no access on this Application Instance",
            "Δεν έχετε πρόσβαση σε αυτό το Instance", "26"),
    APPLICATION_INSTANCE_PLACEMENT_CREATED(
            "Application instance's placement has been created successfully",
            "Το Instance λήφθηκε επιτυχώς", "23"),
    APPLICATION_INSTANCE_SLICE_CREATED("Application instance's sclie has been created successfully",
            "Το Instance λήφθηκε επιτυχώς", "23"),

    RUNTIME_POLICY_NAME_ALPHANUMERIC(
            "The name of a runtime policy must contain only alphanumeric characters",
            "Το όνομα μιας πολιτικής μπορεί να περιέχει μόνο αλφαριθμητικούς χαρακτήρες", "55"),
    RUNTIME_POLICY_WITH_MULTI_EXPRESSION_MUST_HAS_FUNCTION(
            "Using multiple expression you must have function per each expression",
            "Εάν χρησιμοποιείτε πολλαπλές εκρφάσης θα πρέπει η κάθε εκφραση να έχει συνάρτηση", "55"),
    RUNTIME_POLICY_ON_SCALABLE_COMPONENT_MUST_HAS_FUNCTION(
            "Using expression on scalable component you must have function ",
            "Εάν χρησιμοποιείτε εκρφάσης σε scalable component  θα πρέπει η εκφραση να έχει συνάρτηση", "55"),
    RUNTIME_POLICY_COMPONENT_NODE_DOESNT_EXISTS(
            "Component node doesn't exists",
            "Το component node δεν υπάρχει", "60"),
    RUNTIME_POLICY_MUST_HAS_DIMENSION(
            "Runtime policy metric must contain dimension",
            "Η μετρική της πολιτικής πρέπει να περιέχει κάποια διάσταση", "60"),
    RUNTIME_POLICY_ALREADY_EXISTS(
            "A runtime policy with this name already exists for this application instance",
            "Ενεργή πολιτική με αυτό το όνομα υπάρχει ήδη", "56"),
    RUNTIME_POLICY_NOT_EXISTS("This runtime policy doesn't exist for this application instance",
            "Αυτή η πολιτική δεν υπάρχει", "57"),

    SECURITY_POLICY_ALREADY_EXISTS(
            "A security policy with this name already exists for this application instance",
            "Ενεργή security πολιτική με αυτό το όνομα υπάρχει ήδη", "58"),
    SECURITY_POLICY_NAME_ALPHANUMERIC(
            "The name of a security policy must contain only alphanumeric characters",
            "Το όνομα μιας πολιτικής μπορεί να περιέχει μόνο αλφαριθμητικούς χαρακτήρες", "59"),
    SECURITY_POLICY_COMPONENT_NODE_DOESNT_EXISTS(
            "Component node doesn't exists",
            "Το component node δεν υπάρχει", "60"),
    SECURITY_POLICY_COMPONENT_NODE_INSTANCE_DOESNT_EXISTS(
            "Component node instance doesn't exists",
            "Το component node instance δεν υπάρχει", "61"),


    SECURITY_CONFIGURATION_ALREADY_EXISTS(
            "A security configuration with this name already exists for this application instance",
            "Ενεργή security configuration με αυτό το όνομα υπάρχει ήδη", "58"),
    SECURITY_CONFIGURATION_NAME_ALPHANUMERIC(
            "The name of a security configuration must contain only alphanumeric characters",
            "Το όνομα μιας πολιτικής μπορεί να περιέχει μόνο αλφαριθμητικούς χαρακτήρες", "59"),
    SECURITY_CONFIGURATION_COMPONENT_NODE_DOESNT_EXISTS(
            "Component node doesn't exists",
            "Το component node δεν υπάρχει", "60"),
    SECURITY_CONFIGURATION_COMPONENT_NODE_INSTANCE_DOESNT_EXISTS(
            "Component node instance doesn't exists",
            "Το component node instance δεν υπάρχει", "61"),

    PROFILE_ALREADY_EXISTS(
            "A profile with this name already exists for this application instance",
            "Ενεργό προφιλ με αυτό το όνομα υπάρχει ήδη", "62"),
    PROFILE_NAME_ALPHANUMERIC(
            "The name of a profile must contain only alphanumeric characters",
            "Το όνομα ενός προφιλ μπορεί να περιέχει μόνο αλφαριθμητικούς χαρακτήρες", "63"),
    PROFILE_UNSUPPORTED_ALGORITHM(
            "The name algorithm is unsupported",
            "Το όνομα του αλγορίθμου δεν υποστιρίζεται", "64"),
    PROFILE_ALGORITHM_UNSUPPORTED_INPUT(
            "The selected algorithm needs different amount of metrics",
            "O επιλεγμένπος αλγόριθμος απαιτέι διαφορετικο πλήθος μετρικων", "65"),
    PROFILE_NO_VALID_TIME_RANGE(
            "The given time range is wrong",
            "Το ευρος τιμων για τον χρόνο είναι λάθος", "66"),
    PROFILE_DELETE(
            "The profiler deleted successfully! ",
            "To προφιλ έσβησε", "67"),

    VULNERABILITY_ALREADY_EXISTS(
            "The vulnerability already exists! ",
            "Η ευπάθεια υπάρχει ήδη!", "68"
    ),
    VULNERABILITY_NOT_AUTHORIZED("You have no access on this Vulnerability!",
            "Δεν έχετε πρόσβαση σε αυτήν την ευπάθεια!", "69"),

    VULNERABILITY_NOT_EXIST("This Vulnerability doesn't exist",
            "Αυτή η ευπαθεια δεν υπάρχει", "70"),
    TOKEN_DELETED("The token deleted", "Το token διαγραφθηκε", "71"),
    METRIC_FETCHED("Metric has been fetched successfully", "Η μετρική λήφθηκε επιτυχώς", "72"),
    NAMESPACE_FETCHED("Namespace has been fetched successfully", "To namespace λήφθηκε επιτυχώς", "73"),

    ELASTICITY_STRATEGIES_FETCHED("Elasticity strategies has been fetched successfully", "Οι elasticity strategies λήφθηκαν επιτυχώς", "74"),
    SERVICE_GRAPH_FETCHED("Service graph has been fetched successfully", "To service graph λήφθηκε επιτυχώς", "75"),
    SERVICE_GRAPH_APPLIED("Service graph has been applied successfully", "To service graph εφαρμόστηκε επιτυχώς", "76"),
    SERVICE_GRAPH_NOT_FOUND("Service graph not found", "To service graph δεν βρέθηκε", "77"),
    CLUSTER_LABELS_FETCHED("Cluster lables has been fetched successfully", "Tα cluster labels λήφθηκαν επιτυχώς", "73"),


    ENTITIES_FETCHED("Entities have been fetched successfully", "", "100"),

    SLO_NOT_FOUND("Slo not found", "", "101");

    private final String messageEN;
    private final String messageEL;
    private final String code;

    GenericMessage(String messageEN, String messageEL, String code) {
        this.messageEN = messageEN;
        this.messageEL = messageEL;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getMessageEN() {
        return messageEN;
    }

    public String getMessageEL() {
        return messageEL;
    }

    public String getMessage(HttpServletRequest request) {

        String locale = LocaleUtil.fetchLocale(request);

        if (null != locale && locale.contains("el")) {
            return messageEL;
        } else {
            return messageEN;
        }
    }

    public String getMessage(LocaleEnum locale) {
        if (LocaleEnum.EL.equals(locale)) {
            return messageEL;
        } else if (LocaleEnum.EN.equals(locale)) {
            return messageEN;
        } else {
            return messageEN;
        }
    }

}
