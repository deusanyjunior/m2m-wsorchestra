# -*- encoding : utf-8 -*-
class CreateNotes < ActiveRecord::Migration
  def self.up
    create_table :notes do |t|
      t.string :username
      t.integer :note
      t.integer :velocity

      t.timestamps
    end
  end

  def self.down
    drop_table :notes
  end
end
