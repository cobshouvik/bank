import java.util.*;
import java.util.stream.Collectors;

public class Bank {

    static String[][] getInput() {
        String[][] queries = {
            {"CREATE_ACCOUNT", "1", "account1"},
            {"CREATE_ACCOUNT", "2", "account2"},
            {"CREATE_ACCOUNT", "3", "account3"},
            {"DEPOSIT", "4", "account1", "2000"},
            {"DEPOSIT", "5", "account2", "3000"},
            {"DEPOSIT", "6", "account3", "4000"},
            {"TOP_ACTIVITY", "7", "3"},
            {"PAY", "8", "account1", "1500"},
            {"PAY", "9", "account2", "250"},
            {"DEPOSIT", "10", "account3", "250"},
            {"TOP_ACTIVITY", "11", "3"}
        };

        
//         String[][] queries = {
//     {"CREATE_ACCOUNT", "1", "account1"},
//     {"CREATE_ACCOUNT", "2", "account1"},
//     {"CREATE_ACCOUNT", "3", "account2"},
//     {"DEPOSIT", "4", "non-existing", "2700"},
//     {"DEPOSIT", "5", "account1", "2700"},
//     {"PAY", "6", "non-existing", "2700"},
//     {"PAY", "7", "account1", "2701"},
//     {"PAY", "8", "account1", "200"}
// };

//          String[][] queries = {
//     {"CREATE_ACCOUNT", "1", "account1"},
//     {"CREATE_ACCOUNT", "2", "account2"},
//     {"DEPOSIT", "3", "account1", "2000"},
//     {"DEPOSIT", "4", "account2", "3000"},
//     {"TRANSFER", "5", "account1", "account2", "5000"},
//     {"TRANSFER", "16", "account1", "account2", "1000"},
//     {"ACCEPT_TRANSFER", "20", "account1", "transfer1"},
//     {"ACCEPT_TRANSFER", "21", "non-existing", "transfer1"},
//     {"ACCEPT_TRANSFER", "22", "account1", "transfer2"},
//     {"ACCEPT_TRANSFER", "25", "account2", "transfer1"},
//     {"ACCEPT_TRANSFER", "30", "account2", "transfer1"},
//     {"TRANSFER", "40", "account1", "account2", "1000"},
//     {"ACCEPT_TRANSFER", "str(45 + MILLISECONDS_IN_1_DAY)", "account2", "transfer2"},
//     {"TRANSFER", "str(50 + MILLISECONDS_IN_1_DAY)", "account1", "account1", "1000"}
// };

        return queries;
    }


    public static void main(String[] args) {
        String[][] input = getInput();
        BankImpl bank= new BankImpl();
        String[] result = new String[input.length];
        for(int i = 0;i<input.length;i++) {
            String[] command = input[i];
            switch (command[0]) {
                case "CREATE_ACCOUNT":
                    boolean status = bank.createAccount(command[2], Long.valueOf(command[1]));
                    result[i] = String.valueOf(status);
                    break;
                case "DEPOSIT":
                    Optional<Integer> balance1 = bank.deposit(command[2], Integer.valueOf(command[3]), Long.valueOf(command[1]));
                    result[i] = balance1.isEmpty()? "E" : String.valueOf(balance1.get());
                    break;
                case "PAY":
                    Optional<Integer> balance2 = bank.pay(command[2], Integer.valueOf(command[3]), Long.valueOf(command[1]));
                    result[i] = balance2.isEmpty()? "E" : String.valueOf(balance2.get());
                    break;
                case "TOP_ACTIVITY":
                    List<TransactionSumary> transactionSummary = bank.topTransactions(Long.valueOf(command[1]), Integer.valueOf(command[2]));
                    result[i] = transactionSummary.stream()
                            .map(summary -> String.format("%s(%s)", summary.id(), summary.value()))
                            .collect(Collectors.joining(", "));
                    break;
                default:
                System.out.println(command[0]);
                    throw new UnsupportedOperationException();
            }
        }

        for(String res : result) {
            System.out.println(res);
        }
        
    } 
    
}

class BankImpl {
    Map<String, Account> accounts;
    Map<String, List<Transaction>> transactions;

    BankImpl() {
        this.accounts = new HashMap<>();
        this.transactions = new HashMap<>();
    }

    boolean createAccount(String id, long ts) {
        if(this.accounts.containsKey(id)) {
            return false;
        }
        this.accounts.put(id, new Account());
        Transaction createTransaction = new Transaction("CREATE", 0, ts);
        List<Transaction> initTransactions = new ArrayList<>(Arrays.asList(createTransaction));
        this.transactions.put(id, initTransactions);
        return true;
    }

    Optional<Integer> deposit(String id, int value, long ts) {
        if(!this.accounts.containsKey(id)) {
            return Optional.empty();
        }
        int balance = accounts.get(id).deposit(value);
        transactions.get(id).add(new Transaction("DEPOSIT", value, ts));
        return Optional.of(balance);
    }

    Optional<Integer> pay(String id, int value, long ts) {
        if(!this.accounts.containsKey(id) || accounts.get(id).getBalance()<value) {
            return Optional.empty();
        }
        int balance = accounts.get(id).pay(value);
        transactions.get(id).add(new Transaction("PAY", value, ts));
        return Optional.of(balance);
    }

    List<TransactionSumary> topTransactions(long ts, int top) {
        return transactions.keySet()
            .stream()
            .map(k -> new TransactionSumary(k, transactions.get(k)
                    .stream()
                    .filter(t -> t.timestamp() < ts)
                    .map(Transaction::value)
                    .mapToInt(Integer::intValue)
                    .sum()))
            .sorted((a,b) -> b.value() - a.value())
            .limit(top)
            .toList();

    }
}

record TransactionSumary(String id, int value){}; 

class Account{
    int balance;

    Account() {
        this.balance = 0;
    }

    int deposit(int val) {
        this.balance+=val;
        return this.balance;
    }

    int pay(int val) {
        this.balance-=val;
        return this.balance;
    }

    int getBalance() {
        return this.balance;
    }
}

record Transaction(String type, int value, long timestamp){};