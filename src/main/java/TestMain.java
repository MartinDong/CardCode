import java.util.Arrays;

public class TestMain {


    public static void quickSort(int arr[], int low, int high) {
        if (arr == null || arr.length <= 0) {
            return;
        }

        if (low >= high) {
            return;
        }

        int left = low;
        int right = high;
        // 挖坑1：保存基准的值
        int temp = arr[left];

        while (left < right) {
            while (left < right && arr[right] >= temp) {
                right--;
            }
            // 坑2：从后向前找到比较基准小的元素，插入到基准位置坑中
            arr[left] = arr[right];


            while (left < right && arr[left] < temp) {
                left++;
            }
            // 坑3：从前往后找到比基准大的元素，放到刚才挖出的坑2中
            arr[right] = arr[left];
        }
        // 基准值填补到坑3中，准备分治递归快排
        arr[left] = temp;

        System.out.println("-----Sorting：" + Arrays.toString(arr));
        quickSort(arr, low, left - 1);
        quickSort(arr, left + 1, high);
    }

    public static void main(String[] args) {

        int[] arr = new int[]{2, 1, 5, 3, 7, 4, 8, 9, 0, 6};

        quickSort(arr, 0, arr.length);
    }
}
