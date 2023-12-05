package com.example.lab3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class GameView extends View {
    private final static int COLUMNS = 7;
    private final static int ROWS = 7;

    private Cell cells[][];
    private Cell player;
    private Cell exit;
    private float cellSize, hMargin, vMargin;
    private Paint wallPaint, playerPaint, exitPaint;
    private Random rand;
    private int bgColor;
    private boolean finished;

    public GameView(Context context, @Nullable AttributeSet attrs){
        super(context, attrs);
        rand = new Random();

        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(4);
        createCells();

        playerPaint = new Paint();
        playerPaint.setColor(Color.RED);

        exitPaint = new Paint();
        exitPaint.setColor((Color.BLUE));

        bgColor = Color.rgb(rand.nextInt(255), 255, rand.nextInt(255));
    }

    @Override
    protected  void  onDraw(Canvas canvas){
        if (finished){
            bgColor = Color.rgb(rand.nextInt(255), 255, rand.nextInt(255));
            finished = false;
        }
        canvas.drawColor(bgColor);
        int width = getWidth();
        int height = getHeight();

        if(width/height < COLUMNS/ROWS){
            cellSize = width/(COLUMNS+1);
        } else {
            cellSize = height/(ROWS+1);
        }

        hMargin = (width - COLUMNS*cellSize)/2;
        vMargin = (height - ROWS*cellSize)/2;

        canvas.translate(hMargin, vMargin);
        for (int i=0; i<COLUMNS; i++){
            for (int j=0; j<ROWS; j++){
                if(cells[i][j].topWall){
                    canvas.drawLine(i*cellSize, j*cellSize, (i+1)*cellSize, j*cellSize, wallPaint);
                }
                if(cells[i][j].rightWall){
                    canvas.drawLine((i+1)*cellSize, j*cellSize, (i+1)*cellSize, (j+1)*cellSize, wallPaint);
                }
                if(cells[i][j].leftWall){
                    canvas.drawLine(i*cellSize, j*cellSize, i*cellSize, (j+1)*cellSize, wallPaint);
                }
                if(cells[i][j].bottomWall){
                    canvas.drawLine(i*cellSize, (j+1)*cellSize, (i+1)*cellSize, (j+1)*cellSize, wallPaint);
                }
            }
        }

        float rectMargin = cellSize/10;
        canvas.drawRect(player.col*cellSize+rectMargin, player.row*cellSize+rectMargin, (player.col+1)*cellSize-rectMargin, (player.row+1)*cellSize-rectMargin, playerPaint);
        canvas.drawRect(exit.col*cellSize+rectMargin, exit.row*cellSize+rectMargin, (exit.col+1)*cellSize-rectMargin, (exit.row+1)*cellSize-rectMargin, exitPaint);
    }

    @Override
    public  boolean onTouchEvent(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE){
            float x = event.getX();
            float y = event.getY();

            float playerCenterX = hMargin+(player.col+0.5f)*cellSize;
            float playerCenterY = vMargin+(player.col+0.5f)*cellSize;

            float dx = x - playerCenterX;
            float dy = y - playerCenterY;

            if (Math.abs(dx) > cellSize || Math.abs(dy) > cellSize){
                if(Math.abs(dx) > Math.abs(dy)){
                    if (dx > 0){
                        movePlayer(3);
                    } else {
                        movePlayer(2);
                    }
                } else {
                    if (dy > 0){
                        movePlayer(1);
                    } else {
                        movePlayer(0);
                    }
                }
            }
            return true;
        }

        return super.onTouchEvent(event);
    }

    private void movePlayer(int d){
        switch (d){
            case 0:
                if(!player.topWall) {
                    player = cells[player.col][player.row - 1];
                }
                break;
            case 1:
                if(!player.bottomWall) {
                    player = cells[player.col][player.row + 1];
                }
                break;
            case 2:
                if(!player.leftWall) {
                    player = cells[player.col - 1][player.row];
                }
                break;
            case 3:
                if(!player.rightWall) {
                    player = cells[player.col + 1][player.row];
                }
                break;
        }

        if (player == exit){
            finished = true;
            createCells();
        }
        invalidate();
    }
    private Cell randomNeighbour(Cell cell){
        ArrayList<Cell> neighbours = new ArrayList<>();

        if(cell.col>0 && !cells[cell.col-1][cell.row].visited){
            neighbours.add(cells[cell.col-1][cell.row]);
        }
        if(cell.col<COLUMNS-1 && !cells[cell.col+1][cell.row].visited){
            neighbours.add(cells[cell.col+1][cell.row]);
        }
        if(cell.row>0 && !cells[cell.col][cell.row-1].visited){
            neighbours.add(cells[cell.col][cell.row-1]);
        }
        if(cell.row<ROWS-1 && !cells[cell.col][cell.row+1].visited){
            neighbours.add(cells[cell.col][cell.row+1]);
        }
        if (neighbours.size() > 0) {
            int randIndex = rand.nextInt(neighbours.size());
            return neighbours.get(randIndex);
        } else {
            return null;
        }
    }

    private void removeWall(Cell curr, Cell next){
        if (curr.row == next.row+1 && curr.col == next.col){
            curr.topWall = false;
            next.bottomWall = false;
        }

        if (curr.row == next.row-1 && curr.col == next.col){
            curr.bottomWall = false;
            next.topWall = false;
        }

        if (curr.row == next.row && curr.col == next.col+1){
            curr.leftWall = false;
            next.rightWall = false;
        }

        if (curr.row == next.row && curr.col == next.col-1){
            curr.rightWall = false;
            next.leftWall = false;
        }
    }
    private void createCells(){
        Stack<Cell> stack = new Stack<>();
        Cell curr, next;
        cells = new Cell[COLUMNS][ROWS];

        for(int i=0; i<COLUMNS; i++){
            for(int j=0; j<ROWS; j++){
                cells[i][j] = new Cell(i, j);
            }
        }

        player = cells[0][0];
        exit = cells[COLUMNS-1][ROWS-1];

        curr = cells[0][0];
        curr.visited = true;
        do {
            next = randomNeighbour(curr);
            if (next != null) {
                removeWall(curr, next);
                stack.push(curr);
                curr = next;
                curr.visited = true;
            } else {
                curr = stack.pop();
            }
        } while (!stack.empty());
    }

    private class Cell{
        boolean topWall = true;
        boolean rightWall = true;
        boolean leftWall = true;
        boolean bottomWall = true;
        boolean visited = false;
        int col, row;

        public Cell(int col, int row){
            this.col = col;
            this.row = row;
        }
    }
}
