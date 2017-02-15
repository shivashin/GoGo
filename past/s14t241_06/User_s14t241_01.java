package data.strategy.user.past.s14t241_06;

import sys.game.GameBoard;
import sys.game.GameCompSub;
import sys.game.GameHand;
import sys.game.GamePlayer;
import sys.game.GameState;
import sys.struct.GogoHand;
import sys.user.GogoCompSub;

public class User_s14t241_01 extends GogoCompSub {

  //====================================================================
  //  コンストラクタ
  //====================================================================

  public User_s14t241_01(GamePlayer player) {
    super(player);
    name = "s14t241";    // プログラマが入力

  }

  //--------------------------------------------------------------------
  //  コンピュータの着手
  //--------------------------------------------------------------------

  public synchronized GameHand calc_hand(GameState state, GameHand hand) {
    theState = state;
    theBoard = state.board;
    lastHand = hand;

    //--  置石チェック
    init_values(theState, theBoard);

    //--  評価値の計算
    calc_values(theState, theBoard);
    // 先手後手、取石数、手数(序盤・中盤・終盤)で評価関数を変える

    // 評価値の表示
    //showValue();
    //--  着手の決定
    return deside_hand();

  }

  //----------------------------------------------------------------
  //  置石チェック
  //----------------------------------------------------------------

  public void init_values(GameState prev, GameBoard board) {
    this.size = board.SX;
    values = new int[size][size];
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (board.get_cell(i, j) != board.SPACE) {
          values[i][j] = -10;
        } else {
          if (values[i][j] == -10) {
            values[i][j] = 0;
          }
        }
      }
    }
  }

  //----------------------------------------------------------------
  //  評価値の計算
  //----------------------------------------------------------------

  public void calc_values(GameState prev, GameBoard board) {
    int [][] cell = board.get_cell_all();  // 盤面情報
    int mycolor;                  // 自分の石の色
    int myStone = get_mystone(prev);
    int enemyStone = get_enemystone(prev);
    mycolor = role;


    //--  各マスの評価値
    for ( int i = 0; i < size; i++ ) {
      for ( int j = 0; j < size; j++ ) {
        // 埋まっているマスはスルー
        if (values[i][j] < 0) { continue; }
        //-- 自分の手の評価
        values[i][j] = myEvaluation(cell, mycolor, i, j, myStone);
        // もし禁じ手なら
        if ( values[i][j] < 0 ) { continue; }
        //-- 相手の評価
        values[i][j] += enemyEvaluation(cell, mycolor*-1, i, j, enemyStone);
        // ランダム
        if (values[i][j] == 0) {
          int aaa = (int) Math.round(Math.random() * 15);
          if (values[i][j] < aaa) { values[i][j] = aaa; }
        }
        // 四々や四三の判定
        // 飛び三や飛び四の判定
        // 三をどちらで止めるか
      }
    }
    addBoardValues(values);
  }

  //----------------------------------------------------------------
  //  自分の手の評価 
  //----------------------------------------------------------------
  public int myEvaluation(int[][] board, int color, int i, int j, int stone) {
    int myPoint = 0;
    // 禁じ手判定
    if ( isFoul(board, color, i, j) ) { return myPoint = -5000; }
    // 石を10個とって勝利
    if ( stone == 8 && check_rem(board, color*-1, i, j) ) { return myPoint = 1000; }
    // 自分の5連を作る
    if ( check_run(board, color, i, j, 5) ) { return myPoint = 1000; }
    // 自分の飛び5連を作る
    if ( isJumpLen(board, color, i, j, 5) ) { return myPoint = 1000; }
    // 自分の四連を作る → 600;
    if ( check_run(board, color, i, j, 4) ) { return myPoint = 800; }
    // 自分の飛び4連を作る
    if ( isJumpLen(board, color, i, j, 4) ) { return myPoint = 800; }
    // 8個目の石を取る
    if ( stone == 6 && check_rem(board, color*-1, i, j) ) { return myPoint = 600; }
    // 自分の三連を作る → 400;
    if ( check_run(board, color, i, j, 3) ) { return myPoint = 400; }
    // 自分の飛び三連を作る
    if ( isJumpLen(board, color, i, j, 3) ) { return myPoint = 400; }
    // 相手の石を取る → 300;
    if ( check_rem(board, color, i, j) ) { return myPoint = 300; }
    // 値の返却
    return myPoint;
  }

  //----------------------------------------------------------------
  //  相手の手の評価 
  //----------------------------------------------------------------
  public int enemyEvaluation(int[][] board, int color, int i, int j, int stone) {
    int enemyPoint = 0;
    // 敗北阻止(五連) → 800;
    if ( check_run(board, color, i, j, 5) ) { return enemyPoint = 900; }
    // 敗北阻止(飛び5連)
    if ( isJumpLen(board, color, i, j, 5) ) { return enemyPoint = 900; }
    // 敗北阻止10個取られる
    if ( stone == 8 && check_rem(board, color, i, j) ) { return enemyPoint = 900; }
    // 相手の四連を止める → 700;
    if ( check_run(board, color, i, j, 4) ) { return enemyPoint = 700; }
    // 相手の飛び4連を阻止
    if ( isJumpLen(board, color, i, j, 4) ) { return enemyPoint = 700; }
    // 8個目の石を取らせないようにする
    if ( stone == 6 && check_rem(board, color, i, j) ) { return enemyPoint = 500; }
    // 相手の三連を防ぐ → 500;
    if ( check_run(board, color, i, j, 3) ) { return enemyPoint = 500; }
    // 相手の3連を防ぐ
    if ( isJumpLen(board, color, i, j, 3) ) { return enemyPoint = 500; }
    // 相手の石を取る 
    if ( check_rem(board, color, i, j) ) { return enemyPoint = 200; }
    // 値の返却
    return enemyPoint;
  }

  //----------------------------------------------------------------
  //  連の全周チェック
  //----------------------------------------------------------------

  boolean check_run(int[][] board, int color, int i, int j, int len) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( check_run_dir(board, color, i, j, dx, dy, len) ) { return true; }
      }
    }
    return false;
  }

  //----------------------------------------------------------------
  //  連の方向チェック(止連・端連・長連も含む、飛びは無視)
  //----------------------------------------------------------------

  boolean check_run_dir(int[][] board, int color, int i, int j, int dx, int dy, int len) {
    for ( int k = 1; k < len; k++ ) {
      int x = i+k*dx;
      int y = j+k*dy;
      if ( x < 0 || y < 0 || x >= size || y >= size ) { return false; }
      if ( board[i+k*dx][j+k*dy] != color ) { return false; }
    }
    return true;
  }

  //----------------------------------------------------------------
  //  取の全周チェック(ダブルの判定は無し)
  //----------------------------------------------------------------

  boolean check_rem(int [][] board, int color, int i, int j) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( check_rem_dir(board, color, i, j, dx, dy) ) { return true; }
      }
    }
    return false;
  }

  //----------------------------------------------------------------
  //  取の方向チェック
  //----------------------------------------------------------------

  boolean check_rem_dir(int[][] board, int color, int i, int j, int dx, int dy) {
    int len = 3;
    for ( int k = 1; k <= len; k++ ) {
      int x = i+k*dx;
      int y = j+k*dy;
      if ( x < 0 || y < 0 || x >= size || y >= size ) { return false; }
      if ( board[i+k*dx][j+k*dy] != color*-1 ) { return false; }
      if (k == len-1) { color *= -1; }
    }
    return true;
  }
  //----------------------------------------------------------------
  //  着手の決定
  //----------------------------------------------------------------

  public GameHand deside_hand() {
    GogoHand hand = new GogoHand();
    hand.set_hand(0, 0);  // 左上をデフォルトのマスとする
    int value = -1;       // 評価値のデフォルト
    //--  評価値が最大となるマス
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (value < values[i][j]) {
          hand.set_hand(i, j);
          value = values[i][j];
        }
      }
    }
    return hand;
  }
  //------------------------------------------------
  // 評価値の表示
  //------------------------------------------------
  public void showValue() {
    int i, j;
    for ( i = 0; i < size; i++ ) {
      for ( j = 0; j < size; j++ ) {
        System.out.printf("%5d ", values[j][i]);
      }
      System.out.printf("\n");
    }
    System.out.printf("\n");
  }

  //------------------------------------------------
  // 範囲外かの判定
  //------------------------------------------------
  public boolean isOutOfRange(int x, int y) {
    if ( x < 0 || y < 0 || x >= size || y >= size ) {
      return true;
    }
    return false;
  }

  //------------------------------------------------
  // 禁じ手の判定
  //------------------------------------------------
  public boolean isFoul(int[][] board, int color, int i, int j) {
    int k = 0;
    int tmp = 0;
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( isFoulL(board, color, i, j, dx, dy) ) { return true; }
        if ( isFoulT(board, color, i, j, dx, dy) ) { return true; }
        if ( isFoulX(board, color, i, j, dx, dy) ) { return true; }
      }
    }
    return false;
  }
  //------------------------------------------------
  // 禁じ手の判定L字
  //------------------------------------------------
  public boolean isFoulL(int[][] board, int color, int i, int j, int dx0, int dy0) {
    int len = 3;  // 範囲外判定の必要な数
    int serachLen = 2; // 禁じ手判定に必要な連数

    // 以下禁じ手判定
    for ( int dx1 = -1; dx1 <= 1; dx1++ ) {
      for ( int dy1 = -1; dy1 <= 1; dy1++ ) {
        // 元の位地は除外
        if ( dx1 == 0 && dy1 == 0 ) { continue; }
        // 同一方向の除外
        if ( dx0 == dx1 && dy0 == dy1 ) { continue; }
        // 反対方向同士は五連になるので除外
        if ( dx0 == -dx1 && dy0 == -dy1 ) { continue; }
        // 3つ先が範囲外なら除外
        if ( isOutOfRange(i+dx0*len,j+dy0*len) || isOutOfRange(i+dx1*len,j+dy1*len) ) { continue; }
        if ( isLen(board, color, i, j, dx0, dy0, serachLen) && isLen(board, color, i, j, dx1, dy1, serachLen) ) { return true; }
      }
    }
    return false;
  }

  //------------------------------------------------
  // 禁じ手の判定T字
  //------------------------------------------------
  public boolean isFoulT(int[][] board, int color, int i, int j, int dx0, int dy0) {
    // 最初に2連があるかどうかを判断する
    if ( ! isLen(board, color, i, j, dx0, dy0, 2) ) { return false; }

    for ( int dx1 = -1; dx1 <= 1; dx1++ ) {
      for ( int dy1 = -1; dy1 <= 1; dy1++ ) {
        // 元の位地は除外
        if ( dx1 == 0 && dy1 == 0 ) { continue; }
        // 同一方向の除外
        if ( dx0 == dx1 && dy0 == dy1 ) { continue; }
        // 反対方向同士は33の違反にならないので除外
        if ( dx0 == -dx1 && dy0 == -dy1 ) { continue; }
        // 範囲外判定
        if ( isOutOfRange(i+dx1*2, j+dy1*2) || isOutOfRange(i-dx1*2, j-dy1*2) ) { continue; }
        // 2連の左右に石があることと2連の反対側に石が無いか
        if ( isLen(board, color, i, j, dx1, dy1, 1) && isLen(board, color, i, j, -dx1, -dy1, 1) && board[i-dx0][j-dy0] == 0 ) { return true; }
      }
    }
    return false;
  }

  //------------------------------------------------
  // 禁じ手の判定X字
  //------------------------------------------------
  public boolean isFoulX(int[][] board, int color, int i, int j, int dx0, int dy0) {
    int count = 0;  // 個数カウント用
    int serachLen = 1;  // 調べる連の数
    //-- 置く周りに石が何個あるか
    for ( int dx1 = -1; dx1 <= 1; dx1++ ) {
      for ( int dy1 = -1; dy1 <= 1; dy1++ ) {
        // 元の位地は除外
        if ( dx1 == 0 && dy1 == 0 ) { continue; }
        // 同一方向の除外
        if ( dx0 == dx1 && dy0 == dy1 ) { continue; }
        // 反対方向同士は操作対象外除外
        if ( dx0 == -dx1 && dy0 == -dy1 ) { continue; }
        // 石が1連+空白があるか
        if ( isLen(board, color, i, j, dx0, dy0, serachLen) && isLen(board, color, i, j, -dx0, -dy0, serachLen) ) {
          if ( isLen(board, color, i, j, dx1, dy1, serachLen) && isLen(board, color, i, j, -dx1, -dy1, serachLen) ) {
            return true;
          }
        } 
      }
    }
    return false;
  }

  //------------------------------------------------
  // 指定連+空白判定 ++- +++- +- +･･･石  -･･･空白
  //------------------------------------------------
  public boolean isLen(int[][] board, int color, int i, int j, int dx, int dy, int len) {
    int k;
    //-- 指定個数の連続があるか
    for ( k = 1; k <= len; k++ ) {
      // 範囲外かの判定
      if ( !isOutOfRange(i+dx*k,j+dy*k) ) {
        // 自分音石が無かったら
        if ( board[i+dx*k][j+dy*k] != color ) {
          return false;
        }
      }
    }
    //-- 指定個数の連続の後に空白があるか
    // 範囲外の判定
    if ( !isOutOfRange(i+dx*k,j+dy*k) ) {
      // 次の場所に石があったら
      if ( board[i+dx*k][j+dy*k] != 0 ) {
        return false;
      }
    }
    return true;
  }
  //------------------------------------------------
  // 飛びの判定
  //------------------------------------------------
  public boolean isJumpLen(int[][] board, int color, int i, int j, int len) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if (  dx == 0 && dy == 0 ) { continue; }
        for ( int l1 = 1; l1 <= len-2; l1++ ) {
          int l2 = len - l1- 1;
          if ( check_run_dir(board,color, i, j, dx, dy, l1+1) && check_run_dir(board, color, i, j, -dx, -dy, l2+1) ) {
            return true;
          }
        }
      }
    }
    return false;
  }
  //------------------------------------------------
  // 盤面に評価値を付ける
  //------------------------------------------------
  public void addBoardValues(int[][] values) {
    int point[][] = {
      {1,1,1,1,1,1,1,1,1,1,1,1,1},
      {1,2,2,2,2,2,2,2,2,2,2,2,1},
      {1,2,3,3,3,3,3,3,3,3,3,2,1},
      {1,2,3,4,4,4,4,4,4,4,3,2,1},
      {1,2,3,4,5,5,5,5,5,4,3,2,1},
      {1,2,3,4,5,6,6,6,5,4,3,2,1},
      {1,2,3,4,5,6,7,6,5,4,3,2,1},
      {1,2,3,4,5,6,6,6,5,4,3,2,1},
      {1,2,3,4,5,5,5,5,5,4,3,2,1},
      {1,2,3,4,4,4,4,4,4,4,3,2,1},
      {1,2,3,3,3,3,3,3,3,3,3,2,1},
      {1,2,2,2,2,2,2,2,2,2,2,2,1},
      {1,1,1,1,1,1,1,1,1,1,1,1,1}
    };
    for ( int i = 0; i < size; i++ ) {
      for ( int j = 0; j < size; j++ ) {
        values[i][j] += point[i][j];
      }
    }
  }


  //-----------------------------------------------
}


